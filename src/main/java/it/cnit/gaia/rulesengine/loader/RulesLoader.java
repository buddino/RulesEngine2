package it.cnit.gaia.rulesengine.loader;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.Area;
import it.cnit.gaia.rulesengine.model.Fireable;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.rules.CompositeRule;
import it.cnit.gaia.rulesengine.rules.ExpressionRule;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
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
	private final String rulesPackage = "it.cnit.gaia.rulesengine.rules";
	private final String ruleContainerName = "Area";
	//TODO Non serve a niente! Levalo!

	@Autowired
	private OrientGraphFactory graphFactory;

	@Autowired
	private MeasurementRepository measurementRepository;

	private Logger LOGGER = Logger.getLogger("RulesLoader");
	private Map<Long, School> schools = null;
	private OrientGraph tx;

	//TEST
	public void loadSchoolByRid(String rid) {
		tx = graphFactory.getTx();
		OrientVertex sv = tx.getVertex(rid);
		School school = traverseSchool(sv);
		tx.shutdown();
	}

	//////
	public Map<Long, School> loadSchools() {
		if (schools != null) {
			return schools;
		}
		tx = graphFactory.getTx();
		schools = new HashMap<>();
		Iterable<Vertex> schoolVertices = tx.getVerticesOfClass("School");
		for (Vertex sv : schoolVertices) {
			if (sv.getProperty("enabled")) {
				School school = traverseSchool(sv);
				schools.put(school.aid, school);
				tx.commit();
			}
		}
		tx.shutdown();
		measurementRepository.updateMeterMap();
		return schools;
	}

	public boolean reloadSchool(Long id) {
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

	private School traverseSchool(Vertex v) {
		//Check if the given vertex is a School
		OrientVertex schoolVertex = (OrientVertex) v;
		if (!schoolVertex.getProperty("@class").equals("School")) {
			return null;
			//TODO throw new Exception("Not a School Vertex");
		}

		School school = new School();
		school.setName(schoolVertex.getProperty("name"));
		school.rid = schoolVertex.getRecord().getIdentity().toString();
		school.aid = schoolVertex.getProperty("aid");
		school.name = schoolVertex.getProperty("name");
		school.type = "School";

		LOGGER.info("Loading tree for school: " + school.getName());
		Iterable<Vertex> children = schoolVertex.getVertices(Direction.OUT);
		for (Vertex child : children) {
			Fireable f = traverse((OrientVertex) child, school);
			if (f != null)
				school.add(f);
		}
		return school;
	}

	public void reloadAllSchools() {
		schools = null;
	}

	public GaiaRule getRuleForTest(OrientVertex ov) {
		String classname = ov.getProperty("@class");
		Class<?> ruleClass = null;
		try {
			ruleClass = Class.forName(rulesPackage + "." + classname);        //Get correspondant class
		} catch (ClassNotFoundException e) {
			LOGGER.error("Failed initializing " + classname);
			LOGGER.error(e.toString());
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
						//Populate the uri set
						if (f.isAnnotationPresent(URI.class)) {
							measurementRepository.addUri((String) property);
						}
						///
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				} else {
					if (annotation.required())
						LOGGER.error("Field " + f.getName() + " not found in the database [" + ov.getProperty("@rid") + "]");
				}
			}
		}
		return (GaiaRule) rule;
	}

	private Fireable traverse(OrientVertex ov, School school) {
		//If it is a Area
		if (!(Boolean) ov.getProperty("enabled")) {
			LOGGER.debug(ov.getIdentity().toString() + " DISABLED");
			return null;
		}
		String classname = ov.getProperty("@class");
		if (classname.equals(ruleContainerName)) {
			Area area = new Area();                        //Instantiate a ruleSet
			area.rid = ov.getIdentity().toString();
			area.aid = ov.getProperty("aid");
			area.name = ov.getProperty("name");
			area.type = ov.getProperty("type");

			//TODO Fields of the area
			Iterable<Vertex> children = ov.getVertices(Direction.OUT);       //Get all the children
			for (Vertex child : children) {
				Fireable f = traverse((OrientVertex) child, school);
				if (f != null) {
					try {
						if (f.init())
							area.add(f);
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
					}
				}
			}
			return area;
		}

		//If it is a GaiaRule
		else {
			Class<?> ruleClass;
			try {
				ruleClass = Class.forName(rulesPackage + "." + classname);        //Get correspondant class
			} catch (ClassNotFoundException e) {
				LOGGER.error("Failed initializing " + classname);
				LOGGER.error(e.toString());
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
							//Populate the uri set
							if (f.isAnnotationPresent(URI.class)) {
								measurementRepository.addUri((String) property);
							}
							///
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					} else {
						if (annotation.required())
							LOGGER.error("Field " + f.getName() + " not found in the database [" + ov.getProperty("@rid") + "]");
					}
				}
			}
			//EXPRESSION RULE
			if (rule instanceof ExpressionRule) {
				//The rule
				ExpressionRule expressionRule = (ExpressionRule) rule;
				//Matcher
				String uriPattern = "(.*)_uri";
				Pattern pattern = Pattern.compile(uriPattern);
				//Build the rule
				Map<String, Object> properties = ov.getProperties();
				String expression = properties.get("expression").toString();
				expressionRule.expression = expression;
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
			}

			//IF COMPOSITE RULE
			if (rule instanceof CompositeRule) {
				Iterable<Vertex> rules = ov.getVertices(Direction.OUT);

				//Create the rule set
				Set<Fireable> ruleSet = new HashSet<>();
				for (Vertex r : rules) {
					Fireable f = traverse((OrientVertex) r, school);
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
				Field ruleSetField = ReflectionUtils.findField(ruleClass, "ruleSet");
				if (ruleSetField != null) {
					try {
						ruleSetField.set(rule, ruleSet);

					} catch (IllegalAccessException e) {
						LOGGER.error(e.getMessage());
					}
				}

			}
			((GaiaRule) rule).setSchool(school);
			ov.setProperty("school", school.getRid());
			ov.attach(tx);
			ov.save();
			return (Fireable) rule;
		}
	}
}
