import com.google.gson.Gson;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import it.cnit.gaia.rulesengine.event.EventService;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OrientConfiguration.class, EventService.class})
public class TestDB {

	Gson gson = new Gson();

	@Autowired
	OrientGraphFactory ogf;

	@Autowired
	EventService eventService;


	@Test
	public void testDateDatime() {
		DateTime date = new DateTime();
		System.err.println(date.getMillis());
		System.err.println(date.minusHours(1).getMillis());
		System.err.println(date.getMillis());
	}


	@Test
	public void testGraphEvent() {
		ODatabaseDocumentTx rawGraph = ogf.getTx().getRawGraph();
		ODocument doc = new ODocument("GaiaEvent");
		doc.field("Prova", 12);
		doc.save();
		rawGraph.commit().close();
	}

	@Test
	public void testGetEvents() {
		OrientGraphNoTx graph = ogf.getNoTx();
		OCommandSQL command = new OCommandSQL("SELECT * FROM GaiaEvent");
		command.setFetchPlan("*:-1");
		command.setLimit(10);
		Iterable<OrientVertex> result = graph.command(command).execute();
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(result.iterator().next().getRecord().toJSON());
		for (OrientVertex v : result) {
			sb.append("," + v.getRecord().toJSON());
		}
		sb.append("]");
		System.out.println(sb.toString());
	}



}

