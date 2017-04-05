import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.utils.DatabaseSchemaService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OrientConfiguration.class, DatabaseSchemaService.class})
public class SchemaTest {
	@Autowired
	OrientGraphFactory ogf;

	@Autowired
	DatabaseSchemaService dbs;


	@Test
	public void testAddToSchema() {
		final String classname = "PowerFactor2";
		final String rulesPackage = "it.cnit.gaia.rulesengine.rules.";
		Class<?> ruleClass = null;
		OSchemaProxy schema = ogf.getDatabase().getMetadata().getSchema();

		try {
			ruleClass = Class.forName(rulesPackage + classname);
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

	@Test
	public void testService(){
		dbs.addClassToSchema("PowerFactor2");
	}

	@Test
	public void testDefault(){
		String classname = "PowerFactor2";
		String properyName = "suggestion";
		String defaultValue = "VALORE DI DEFAULT";
		OSchemaProxy schema = ogf.getDatabase().getMetadata().getSchema();
		OProperty property = schema.getClass(classname).getProperty(properyName);
		property.setDefaultValue(defaultValue);
		Assert.assertEquals(schema.getClass(classname).getProperty(properyName).getDefaultValue(), defaultValue);
	}

	@Test
	public void tetsOproperty(){
	}

	private void printProperty(OProperty property) {
		String row = String.format("%s : %s", property.getName(), property.getDefaultValue());
		System.out.println(row);
	}


}
