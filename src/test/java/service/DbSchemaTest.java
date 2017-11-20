package service;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchemaProxy;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OrientConfiguration.class})
public class DbSchemaTest {

	@Autowired
	OrientGraphFactory ogf;

	@Test
	public void test(){
		OSchemaProxy schema = ogf.getDatabase().getMetadata().getSchema();
		Collection<OClass> classes = schema.getClasses();
		List<OClass> gaiaRules = classes.stream().filter(c -> c.isSubClassOf("GaiaRule")).collect(Collectors.toList());
		System.out.println(gaiaRules);
	}
}
