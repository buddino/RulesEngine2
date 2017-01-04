import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OrientConfiguration.class, RulesLoader.class})
public class TestDB {
	@Autowired
	OrientGraph orientdb;

	@Autowired
	RulesLoader rulesLoader;

	@Test
	public void test(){
		OrientVertex vertex = orientdb.getVertex("#21:0");
		Object description = vertex.getProperty("description");
	}

	@Test
	public void test2(){
		Iterable<Vertex> vertices = orientdb.getVerticesOfClass("GaiaRuleSet");
		vertices.forEach( v -> System.out.println(v));
	}

	@Test
	public void testLoader(){

	}
}
