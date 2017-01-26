package it.cnit.gaia.rulesengine.loader;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.Fireable;
import it.cnit.gaia.rulesengine.model.GaiaRuleSet;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.model.annotation.FromConfiguration;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.rules.CompositeRule;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Service
public class RulesLoader {
	private final String rulesPackage = "it.cnit.gaia.rulesengine.rules";
	private final String ruleContainerName = "GaiaRuleSet";

	@Autowired
	private OrientGraphFactory graphFactory;

	@Autowired
	private MeasurementRepository measurementRepository;

	private Logger LOGGER = Logger.getLogger("RulesLoader");
	private Fireable root = null;
	private String rootId = null;

	public Set<School> getSchools(){
		Set<School> schools = new HashSet<>();
		OrientGraph tx = graphFactory.getTx();
		Iterable<Vertex> school = tx.getVerticesOfClass("School");
		for(Vertex v : school){
			OrientVertex ov = (OrientVertex) v;
			School s = new School();
			s.setName(ov.getProperty("name"));
			LOGGER.info("Loading tree for school: " + s.getName());
			OrientVertex rootVertex = (OrientVertex) v.getVertices(Direction.OUT).iterator().next(); //Exeption
			Fireable rootFireable = getRuleTree(rootVertex);
			s.setRoot(rootFireable);
			schools.add(s);
		}
		return schools;
	}


	public Fireable getRuleTree(String rootId) {
		OrientGraph orientdb = graphFactory.getTx();
		if (root == null || !this.rootId.equals(rootId)) {
			LOGGER.info("Loading tree for root: " + rootId);
			OrientVertex v = orientdb.getVertex(rootId);
			this.rootId = rootId;
			root = traverse(v);
			measurementRepository.updateMeterMap(); //Update the meter map, i.e. retrieve resource IDs
			return root;
		} else {
			return root;
		}
	}

	public Fireable getRuleTree(OrientVertex v) {
		OrientGraph orientdb = graphFactory.getTx();
		this.rootId = v.getIdentity().toString();
		LOGGER.info("Loading tree for root: " + rootId);
		root = traverse(v);
		measurementRepository.updateMeterMap(); //Update the meter map, i.e. retrieve resource IDs
		return root;
	}

	public Fireable getRoot() {
		return root;
	}

	public void updateRuleTree() {
		this.root = null;
	}


	private Fireable traverse(Vertex v) {
		//If it is a GaiaRuleSet
		String classname = v.getProperty("@class");
		if (classname.equals(ruleContainerName)) {

			GaiaRuleSet ruleSet = new GaiaRuleSet();                        //Istantiate a ruleSet
			ruleSet.rid = v.getId().toString();
			//
			Iterable<Vertex> children = v.getVertices(Direction.OUT);       //Get all the children
			for (Vertex child : children) {
				Fireable f = traverse(child);
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
			String rid = v.getId().toString();
			try {
				Field idField = ruleClass.getField("rid");
				idField.set(rule, rid);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
			for (Field f : fields) {
				if (f.isAnnotationPresent(FromConfiguration.class)) {
					Object property = v.getProperty(f.getName());
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
						LOGGER.error("Field " + f.getName() + " not found in the database [" + classname + "]");
					}
				}
			}
			//IF COMPOSITE RULE
			if (rule instanceof CompositeRule) {
				Iterable<Vertex> rules = v.getVertices(Direction.OUT);

				//Create the rule set
				Set<Fireable> ruleSet = new HashSet<>();
				for (Vertex r : rules) {
					Fireable f = traverse(r);
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
			return (Fireable) rule;
		}
	}
}
