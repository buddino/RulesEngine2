import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OrientConfiguration.class})
public class TestDB {


	@Autowired
	OPartitionedDatabasePool eventDbPool;

	@Autowired
	OrientGraphFactory ogf;

	@Test
	public void testEventLog() {
		ODatabaseDocumentTx db = eventDbPool.acquire();
		DateTime date = new DateTime();
		for (int i = 0; i < 105120; i++) {
			if (i % 1000 == 0)
				System.out.println(i);
			date = date.minusHours(1);
			ODocument event = new ODocument("GaiaEvent");
			event.field("name", "Prova").field("timestamp", date.toDate()).field("value", Math.random()).field("class", this.getClass().getName());
			event.save();
		}
		db.close();
	}

	@Test
	public void testRead() {

	}

	@Test
	public void testDateDatime() {
		DateTime date = new DateTime();
		System.err.println(date.getMillis());
		System.err.println(date.minusHours(1).getMillis());
		System.err.println(date.getMillis());
	}

	@Test
	public void timerange() {
		ODatabaseDocumentTx db = eventDbPool.acquire();
		OFunction timerangeSummary = db.getMetadata().getFunctionLibrary().getFunction("summary");
		System.out.println(timerangeSummary);
	}


	@Test
	public void schema() throws IOException {
		OrientGraphNoTx graph_c = ogf.getNoTx();

		OClass day_c = graph_c.createVertexType("Day", "V");
		day_c.createProperty("log", OType.EMBEDDEDLIST, OType.DATETIME);

		OClass month_c = graph_c.createVertexType("Month", "V");
		month_c.createProperty("day", OType.LINKMAP, day_c);

		OClass year_c = graph_c.createVertexType("Year", "V");
		year_c.createProperty("value", OType.INTEGER);
		year_c.createProperty("month", OType.LINKMAP, month_c);

		graph_c.commit();
	}

	@Test
	public void initLogDb() {
		OrientGraph graph = ogf.getTx();
		for (int yy = 2017; yy <= 2017; yy++) {
			Map<Integer, OrientVertex> months = new HashMap<>();
			for (int mm = 0; mm < 12; mm++) {
				Map<Integer, OrientVertex> days = new HashMap<>();
				for (int dd = 1; dd < 32; dd++) {
					OrientVertex day = graph.addVertex("class:Day");
					days.put(dd, day);
				}
				OrientVertex month = graph.addVertex("class:Month");
				month.setProperties("day", days);
				months.put(mm, month);
			}
			OrientVertex year = graph.addVertex("class:Year");
			year.setProperties("value", yy);
			year.setProperties("month", months);
		}

		graph.commit();
		graph.shutdown();
	}

	@Test
	public void eventlog() {
		ODatabaseDocumentTx db = ogf.getTx().getRawGraph();
		ODocument hourlyLog = ogf.getTx().getVertex("#58:0").getRecord();

		List<Date> hour = hourlyLog.field("logs");
		for (int i = 0; i < 12; i++) {
			hour.add(new Date());
		}
		hourlyLog.field("logs", hour);
		hourlyLog.save();
		db.commit();
		db.close();
	}



}

