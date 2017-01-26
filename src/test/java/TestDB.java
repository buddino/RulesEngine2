import com.google.gson.Gson;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import it.cnit.gaia.rulesengine.event.EventService;
import it.cnit.gaia.rulesengine.model.School;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

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
		doc.field("timestamp", new Date());
		doc.field("rule", "#21:0");
		doc.save();
		rawGraph.commit().close();
	}

	@Test
	public void testGetEvents() {
		ODatabaseDocumentTx db = ogf.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("SELECT * FROM GaiaEvent ORDER BY timestamp DESC");
		query.setFetchPlan("*:-1");
		query.setLimit(10);
		List<ODocument> result = db.query(query);
		StringJoiner sj = new StringJoiner(",", "[", "]");
		result.forEach(doc -> sj.add(doc.toJSON()));
		System.out.println(sj.toString());
	}

	 @Test
	public void getEventsOfRule(){
		 OrientGraph tx = ogf.getTx();
		 Iterable<Vertex> school = tx.getVerticesOfClass("School");
		 for(Vertex v : school){
			 OrientVertex ov = (OrientVertex) v;
			 School s = new School();
			 s.setName(ov.getProperty("name"));
			 Vertex root = v.getVertices(Direction.OUT).iterator().next(); //Exeption
			 System.out.println(root);
		 }
	 }




}

