package it.cnit.gaia.rulesengine.utils;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Service
public class DatabaseSchemaService {
	private String rulesPackage = "it.cnit.gaia.rulesengine.rules";

	@Autowired
	private OrientGraphFactory ogf;

	public void addClassToSchema(String classname) {
		Class<?> ruleClass = null;
		OSchemaProxy schema = ogf.getDatabase().getMetadata().getSchema();

		try {
			ruleClass = Class.forName(rulesPackage + "." + classname);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		OClass parent = schema.getClass("GaiaRule");
		OClass aClass = schema.createClass(classname, parent);
		Field[] fields = ruleClass.getDeclaredFields();

		//Get fields of the class
		for (Field f : fields) {
			if (f.isAnnotationPresent(LoadMe.class)) {
				//Create the property
				OProperty property = aClass.createProperty(f.getName(), OType.getTypeByClass(f.getType()));
				if (f.getAnnotation(LoadMe.class).required()) {
					//Set MANDATORY if required by the class
					property.setMandatory(true);
				}
			}
		}
	}

	public void setDefaultForPropertyInClass(String classname, String propertyName, Object defaultValue){
		OSchemaProxy schema = ogf.getDatabase().getMetadata().getSchema();
		OProperty property = schema.getClass(classname).getProperty(propertyName);
		property.setDefaultValue(defaultValue.toString());
	}

	public Map<String,Object> getDefaultForClass(String classname){
		Map<String,Object> defaults = new HashMap<>();
		OSchemaProxy schema = ogf.getDatabase().getMetadata().getSchema();
		schema.getClass(classname).properties().forEach(e -> defaults.put(e.getName(),e.getDefaultValue()));
		return defaults;
	}

	//Riguarda
	public Map<String,Map<String,Object>> getClassSchema(String classname){
		Map<String,Map<String,Object>> defaults = new HashMap<>();
		OSchemaProxy schema = ogf.getDatabase().getMetadata().getSchema();
		schema.getClass(classname).properties().forEach(p -> {
			Map<String, Object> map = new HashMap<>();
			map.put("mandatory", p.isMandatory());
			map.put("regex",p.getRegexp());
			map.put("default",p.getDefaultValue());
			defaults.put(p.getName(),map);
		});
		return defaults;
	}


}

