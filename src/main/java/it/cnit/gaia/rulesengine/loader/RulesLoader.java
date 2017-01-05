package it.cnit.gaia.rulesengine.loader;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.model.Fireable;
import it.cnit.gaia.rulesengine.model.FromConfiguration;
import it.cnit.gaia.rulesengine.model.GaiaRuleSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

@Service
public class RulesLoader {

	private final String rulesPackage = "it.cnit.gaia.rulesengine.rules";
	private final String ruleContainerName = "GaiaRuleSet";
	private Fireable root = null;

	@Autowired
	OrientGraphFactory graphFactory;

	public Fireable getRuleTree(String rootId) {
		OrientGraph orientdb = graphFactory.getTx();
		if (root == null) {
			OrientVertex v = orientdb.getVertex(rootId);
			root = traverse(v);
			return root;
		} else {
			return root;
		}
	}

	//TODO Check if this can create problem if the root object is being used
	public void updateRuleTree(String rootId){
		OrientGraph orientdb = graphFactory.getTx();
		OrientVertex v = orientdb.getVertex(rootId);
		Fireable f = traverse(v);
		root = f;
	}

	private Fireable traverse(Vertex v) {
		//If it is a GaiaRuleSet
		String classname = v.getProperty("@class");
		if (classname.equals(ruleContainerName)) {

			GaiaRuleSet ruleSet = new GaiaRuleSet();                        //Istantiate a ruleSet
			//TODO Add the id to the rule
			String id = v.getProperty("@rid").toString();
			//TODO Setter or access field
			ruleSet.rid = id;
			//
			Iterable<Vertex> children = v.getVertices(Direction.OUT);       //Get all the children
			for (Vertex child : children) {
				Fireable f = traverse(child);
				ruleSet.add(f);
			}
			return ruleSet;
		}

		//If it is a GaiaRule
		else {

			Class<?> ruleClass = null;
			try {
				ruleClass = Class.forName(rulesPackage + "." + classname);        //Get correspondant class
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			Field[] fields = ruleClass.getFields();                //Get fields of the class
			Object rule = null;
			try {
				rule = ruleClass.newInstance();                             //Istantiate the Rule
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			//TODO Add the id to the rule
			String rid = v.getProperty("@rid").toString();
			try {
				Field idField = ruleClass.getField("rid");
				idField.set(rule, rid);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			//
			for (Field f : fields) {
				if (f.isAnnotationPresent(FromConfiguration.class)) {
					Object property = v.getProperty(f.getName());
					if (property != null && !property.equals("")) {
						try {
							f.set(rule, property);
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					} else {
						System.err.println("Null property");
					}
				}
			}
			//IF REPEATING RULE
			if(classname.equals("RepeatingRule")){
				Vertex relatedRule = v.getVertices(Direction.OUT).iterator().next();
				Fireable f = traverse(relatedRule);
				try {
					ruleClass.getField("rule").set(rule, f);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				}
			}
			return (Fireable) rule;
		}
	}
}
