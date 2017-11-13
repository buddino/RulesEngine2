package it.cnit.gaia.rulesengine.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.model.Area;
import it.cnit.gaia.rulesengine.model.Fireable;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.model.exceptions.RulesLoaderException;
import it.cnit.gaia.rulesengine.rules.CompositeRule;
import it.cnit.gaia.rulesengine.rules.ExpressionRule;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RulesLoader {
	public static final String rulesPackage = "it.cnit.gaia.rulesengine.rules";
	private final String ruleContainerName = "Area";
	//TODO Non serve a niente! Levalo!
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private OrientGraphFactory graphFactory;
	@Autowired
	private MeasurementRepository measurementRepository;
	private Map<Long, School> schools = null;
	private Map<Long, School> schools_tmp = null;
	private Map<String, GaiaRule> ruleMap;
	private Map<Long, Area> areaMap;
	private OrientGraph tx; //Riguarda
	private ObjectMapper mapper = new ObjectMapper();

	//TEST
	public void loadSchoolByRid(String rid) throws RulesLoaderException {
		tx = graphFactory.getTx();
		OrientVertex sv = tx.getVertex(rid);
		School school = traverseSchool(sv);
		tx.shutdown();
	}

	public Map<Long, School> getSchools() {
		return schools_tmp;
	}

	//////
	public Map<Long, School> loadSchools() {
		if (schools != null) {
			return schools;
		}
		ruleMap = new HashMap<>();
		areaMap = new HashMap<>();
		LOGGER.info("Reloading schools");
		tx = graphFactory.getTx();
		schools = new HashMap<>();
		Iterable<Vertex> schoolVertices = tx.getVerticesOfClass("School");
		for (Vertex sv : schoolVertices) {
			if (sv.getProperty("enabled")) {
				School school = null;
				try {
					school = traverseSchool(sv);
				} catch (RulesLoaderException e) {
					e.printStackTrace();
				}
				//If the schools variable in nulled before complte loading
				if (schools != null)
					schools.put(school.aid, school);
				else {
					LOGGER.warn("Forced reloading while loading");
					tx.rollback();
					return loadSchools();
				}
			}
		}
		tx.commit();
		tx.shutdown();
		measurementRepository.updateMeterMap();
		schools_tmp = schools;
		return schools;
	}

	public School getSchool(Long id) throws RulesLoaderException {
		if (schools == null || schools.size() == 0) {
			throw new RulesLoaderException("No school has been loaded. Try loading the school structure before gettin a school by id [loadSchools()]");
		}
		School s = schools.get(id);
		if (s == null) {
			throw new RulesLoaderException("School with id [" + id + "] not found");
		}
		return s;
	}

	public boolean reloadSchool(Long id) throws RulesLoaderException {
		graphFactory.getDatabase().getLocalCache().invalidate();
		if (schools.containsKey(id)) {
			School school = schools.get(id);
			Vertex schoolVertex = graphFactory.getTx().getVertex(school.getRid());
			LOGGER.info("Reloading tree for school: " + school.getName());
			school = traverseSchool(schoolVertex);
			schools.put(id, school);
			measurementRepository.updateMeterMap();
			return true;
		}
		return false;
	}

	private School traverseSchool(Vertex v) throws RulesLoaderException {
		//Check if the given vertex is a School
		OrientVertex schoolVertex = (OrientVertex) v;
		if (!schoolVertex.getProperty("@class").equals("School")) {
			throw new RulesLoaderException("The vertex is not of class \"School\"");
		}
		School school = new School();
		school.lat = schoolVertex.getProperty("lat");
		school.lon = schoolVertex.getProperty("lon");
		school.setName(schoolVertex.getProperty("name"));
		school.type = "School";
		school.sqmt = schoolVertex.getProperty("sqmt");
		school.country = schoolVertex.getProperty("country");
		school.people = schoolVertex.getProperty("people");
		buildArea(schoolVertex, school, school);        //Resuse the build area method
		return school;
	}

	public void reloadAllSchools() {
		//graphFactory.getDatabase().getLocalCache().invalidate();
		schools = null;
	}

	public GaiaRule getRuleForTest(OrientVertex ov) {
		String classname = ov.getProperty("@class");
		Class<?> ruleClass = null;
		try {
			ruleClass = Class.forName(rulesPackage + "." + classname);        //Get correspondant class
		} catch (ClassNotFoundException e) {
			LOGGER.error("Failed initializing " + classname, e);
			return null;
		}
		Field[] fields = ruleClass.getFields();                //Get fields of the class
		Object rule = null;
		try {
			rule = ruleClass.newInstance();                             //Istantiate the Rule
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		//Add to the rule its @rid for fast retrieval
		String rid = ov.getId().toString();
		try {
			Field idField = ruleClass.getField("rid");
			idField.set(rule, rid);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		for (Field f : fields) {
			if (f.isAnnotationPresent(LoadMe.class)) {
				LoadMe annotation = f.getAnnotation(LoadMe.class);
				Object property = ov.getProperty(f.getName());
				if (property != null && !property.equals("")) {
					try {
						f.set(rule, property);
						//Populate the power_uri set
						if (f.isAnnotationPresent(URI.class)) {
							measurementRepository.addUri((String) property);
						}
						///
					} catch (IllegalAccessException e) {
						LOGGER.error(e.getMessage(), e);
					}
				} else {
					if (annotation.required())
						LOGGER.error("Field " + f.getName() + " not found in the database [" + ov
								.getProperty("@rid") + "]");
				}
			}
		}
		if (classname.equals(ExpressionRule.class.getSimpleName())) {
			Area tempArea = new Area();
			tempArea.aid = -1L;
			buildExpressionRule(rule, ov, tempArea);
		}
		return (GaiaRule) rule;
	}

	private GaiaRule buildRule(OrientVertex ov, String classname, School school) {
		Class<?> ruleClass;
		try {
			//Load the right class adding the package name to the classname
			ruleClass = Class.forName(rulesPackage + "." + classname);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Failed initializing " + classname);
			LOGGER.error(e.toString());
			return null;
		}
		//Get fields of the class
		Field[] fields = ruleClass.getFields();
		//At the beginning the rule is a general Object
		Object rule = null;
		//Try to instantiate the rule
		try {
			rule = ruleClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		//Set the rule @rid for a fast retrieval
		//@rid is the identifier used by the database
		String rid = ov.getId().toString();
		try {
			Field idField = ruleClass.getField("rid");
			idField.set(rule, rid);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}

		//Iterate over the fields of the rule and fill
		//with the values retrived from the database
		fillOutRuleAttributes(ov, rule, fields);

		//Set the school attribute of the rule and store it in the database
		((GaiaRule) rule).setSchool(school);

		ov.setProperty("school", school.getRid());
		ov.attach(tx);
		ov.save();
		return (GaiaRule) rule;
	}

	private Fireable traverse(OrientVertex ov, School school, Area area) {
		//Check if the Fireable is enabled
		if (ov.getProperty("enabled") == null || !(Boolean) ov.getProperty("enabled")) {
			LOGGER.debug(ov.getIdentity().toString() + " DISABLED");
			return null;
		}
		String classname = ov.getProperty("@class");
		//If the Fireable is a container i.e. an Area
		//return the built area
		if (classname.equals(ruleContainerName)) {
			return buildArea(ov, school);
		}
		//If the Fireable is a rule (GaiaRule)
		GaiaRule rule = buildRule(ov, classname, school);
		//Additional logic for: EXPRESSION RULE
		if (rule instanceof ExpressionRule) {
			buildExpressionRule(rule, ov, area);
		}

		//Additional logic for: COMPOSITE RULE
		if (rule instanceof CompositeRule) {
			buildCompositeRule(rule, ov, school, area);
		}
		rule.setArea(area);
		//Add the rule to the rule map
		ruleMap.put(rule.rid.replace("#", ""), rule);
		return rule;
	}

	/**
	 * Fill the Java rule object's fields (annotated with @LoadMe) retrieving the values from the database
	 * If a field is annotated as @URI its value will be also added to the URIs collection
	 * (to speedup the measurements gathering)
	 *
	 * @param ov     The OrientVertex associtaed with the instance of the rule
	 * @param rule   The instance of the rule to be filled
	 * @param fields The array of Fields of the rule's class
	 */
	private void fillOutRuleAttributes(OrientVertex ov, Object rule, Field[] fields) {
		for (Field f : fields) {
			if (f.isAnnotationPresent(LoadMe.class)) {
				LoadMe annotation = f.getAnnotation(LoadMe.class);
				Object property = ov.getProperty(f.getName());
				if (property != null && !property.equals("")) {
					try {
						f.set(rule, property);
						//Populate the power_uri set
						if (f.isAnnotationPresent(URI.class)) {
							measurementRepository.addUri((String) property);
						}
						///
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				} else {
					if (annotation.required())
						LOGGER.error("Field " + f.getName() + " not found in the database [" + ov
								.getProperty("@rid") + "]");
				}
			}
		}
	}

	/**
	 * Build the Area object by traversing, initializing and connecting all the children Fireables
	 * This is done by calling recursively the traverse(OrientVertex, School) function
	 *
	 * @param ov     The OrientVertex associtaed with the instance of the rule
	 * @param school The Java object associated with the School
	 * @return The created Area object
	 */
	private Area buildArea(OrientVertex ov, School school) {
		Area area = new Area();
		//Create the area
		return buildArea(ov, school, area);
	}

	private Area buildArea(OrientVertex ov, School school, Area area) {
		area.rid = ov.getIdentity().toString();
		area.aid = ov.getProperty("aid");
		area.name = ov.getProperty("name");
		area.type = ov.getProperty("type");
		//TODO Json field
		//Create a map from the json field
		/*
		try {
			String json = ov.getProperty("json");
			if (json != null) {
				JsonNode jsonNode = mapper.readTree(json);
				Map fields = mapper.convertValue(jsonNode, Map.class);
				if(fields!=null)
					area.metadata.putAll(fields);
			}

		} catch (IOException e) {
			LOGGER.warn("Unreadable json field in area: " + area.aid);
		}
		*/
		Iterable<Vertex> children = ov.getVertices(Direction.OUT);      //Get all the children
		for (Vertex child : children) {
			Fireable f = traverse((OrientVertex) child, school, area);
			if (f != null) {
				try {
					if (f.init())
						area.add(f);
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
		area.setSchool(school);
		if (school.aid != area.aid)
			areaMap.put(area.aid, area);
		return area;
	}

	private void buildCompositeRule(Object rule, OrientVertex ov, School school, Area area) {
		Iterable<Vertex> rules = ov.getVertices(Direction.OUT);
		//Create the rule set
		Set<Fireable> ruleSet = new HashSet<>();
		for (Vertex r : rules) {
			Fireable f = traverse((OrientVertex) r, school, area);
			if (f != null) {
				try {
					if (f.init())
						ruleSet.add(f);
					else
						LOGGER.error("Failed initializing " + r.toString());
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
				}
			}
		}

		//Inject the rule set into the CompositeRule
		Field ruleSetField = ReflectionUtils.findField(rule.getClass(), "ruleSet");
		if (ruleSetField != null) {
			try {
				ruleSetField.set(rule, ruleSet);

			} catch (IllegalAccessException e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private GaiaRule buildExpressionRule(Object rule, OrientVertex ov, Area area) {
		//The rule
		ExpressionRule expressionRule = (ExpressionRule) rule;
		expressionRule.setArea(area);
		//Matcher
		String uriPattern = "(.*)_uri";
		Pattern pattern = Pattern.compile(uriPattern);
		//Build the rule
		Map<String, Object> properties = ov.getProperties();
		for (String key : properties.keySet()) {
			Matcher m = pattern.matcher(key);
			if (m.lookingAt()) {
				String variable = m.group(1);
				if (properties.get(key) != null) {
					String uri = properties.get(key).toString();
					measurementRepository.addUri(uri);
					expressionRule.variable2uri.put(variable, uri);
				}
			}
			if (properties.get(key) != null) {
				String stringValue = properties.get(key).toString();
				if (NumberUtils.isNumber(stringValue)) {
					double value = Double.parseDouble(stringValue);
					expressionRule.fields.put(key, value);
				}
			}
		}
		return expressionRule;
	}

	public GaiaRule getGaiaRuleInstance(String rid) {
		return ruleMap.get(rid);
	}

	public Map<String, GaiaRule> getRuleMap() {
		return ruleMap;
	}

	public Map<Long, Area> getAreaMap() {
		return areaMap;
	}
}
