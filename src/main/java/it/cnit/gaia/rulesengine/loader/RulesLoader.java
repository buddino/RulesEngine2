package it.cnit.gaia.rulesengine.loader;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
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

	@Autowired
	OrientGraph orientdb;

	public Fireable createRulesTree(Object rootId){
		OrientVertex v = orientdb.getVertex(rootId);
		return traverse(v);
	}

	private Fireable traverse(Vertex v){
		//If it is a GaiaRuleSet
		if( v.getProperty("@class").equals(ruleContainerName) ){
			GaiaRuleSet ruleSet = new GaiaRuleSet();                        //Istantiate a ruleSet
			Iterable<Vertex> children = v.getVertices(Direction.OUT);       //Get all the children
			for(Vertex child : children){
				Fireable f = traverse(child);
				ruleSet.add(f);
			}
			return ruleSet;
		}
		//If it is a GaiaRule
		else {
			String classname = v.getProperty("@class");
			Class<?> ruleClass = null;
			try {
				ruleClass = Class.forName(rulesPackage+"."+classname);        //Get correspondant class
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
			for( Field f : fields ){
				if( f.isAnnotationPresent(FromConfiguration.class)) {
					Object property = v.getProperty(f.getName());
					if (property != null && !property.equals("")) {
						try {
							f.set(rule, property);
						} catch (IllegalAccessException e) {
							e.printStackTrace();
						}
					}
					else {
						System.err.println("Null property");
					}
				}
			}
			return (Fireable) rule;
		}
	}
}
