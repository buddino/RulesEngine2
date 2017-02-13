package it.cnit.gaia.rulesengine.loader;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.Fireable;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.GaiaRuleSet;
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
	private final String ruleContainerName = "GaiaRuleSet";
	//TODO Non serve a niente! Levalo!

	@Autowired
	private OrientGraphFactory graphFactory;

	@Autowired
	private MeasurementRepository measurementRepository;

	private Logger LOGGER = Logger.getLogger("RulesLoader");
	private Map<String, School> schools = null;

	private OrientGraph tx;


	public Map<String, School> loadSchools() {
		if (schools != null) {
			return schools;
		}
		tx = graphFactory.getTx();
		schools = new HashMap<>();
		Iterable<Vertex> schoolVertices = tx.getVerticesOfClass("School");
		for (Vertex sv : schoolVertices) {
			if( sv.getProperty("enabled")) {
				School school = traverseSchool(sv);
				schools.put(school.getId(), school);
				tx.commit();
			}
		}
		tx.shutdown();
		measurementRepository.updateMeterMap();
		return schools;
	}

	public boolean reloadSchool(String id) {
		if (schools.containsKey(id)) {
			School school = schools.get(id);
			Vertex schoolVertex = graphFactory.getTx().getVertex(school.getRid());
			LOGGER.info("Reloading tree for school: " + school.getName());
			school = traverseSchool(schoolVertex);
			schools.put(id,school);
			measurementRepository.updateMeterMap();
			return true;
		}
		return false;
	}

	private School traverseSchool(Vertex v) {
		OrientVertex ov = (OrientVertex) v;
		School s = new School();
		s.setName(ov.getProperty("name"));
		s.setRid(ov.getRecord().getIdentity().toString());
		s.setId(ov.getProperty("id")); //Riguarda Check id here?
		LOGGER.info("Loading tree for school: " + s.getName());
		try {
			OrientVertex rootVertex = (OrientVertex) v.getVertices(Direction.OUT).iterator().next();
			Fireable rootFireable = traverse(rootVertex, s);
			s.setRoot(rootFireable);
			return s;
		} catch (NullPointerException e) {
			LOGGER.error("Could not find the root rule for school: " + s.getId());
		}
		return null;
	}

	public void reloadAllSchools() {
		schools = null;
	}


	private Fireable traverse(Vertex v, School school) {
		//If it is a GaiaRuleSet
		OrientVertex ov = (OrientVertex) v;
		String classname = ov.getProperty("@class");
		if (classname.equals(ruleContainerName)) {
			GaiaRuleSet ruleSet = new GaiaRuleSet();                        //Instantiate a ruleSet
			ruleSet.rid = ov.getId().toString();
			//
			Iterable<Vertex> children = ov.getVertices(Direction.OUT);       //Get all the children
			for (Vertex child : children) {
				Fireable f = traverse(child, school);
				if (f != null) {
					if (f.init())
						ruleSet.add(f);
				}
			}
			return ruleSet;
		}

		//If it is a GaiaRule
		else {

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
							LOGGER.error("Field " + f.getName() + " not found in the database [" + v.getProperty("@rid") + "]");
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
					Fireable f = traverse(r, school);
					if (f != null) {
						if (f.init())
							ruleSet.add(f);
						else
							LOGGER.error("Failed initializing " + r.toString());
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
