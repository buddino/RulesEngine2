import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OrientConfiguration.class})
public class TestDB {


	@Autowired
	OPartitionedDatabasePool eventDbPool;

	@Test
	public void testEventLog(){
		ODatabaseDocumentTx db = eventDbPool.acquire();
		DateTime date = new DateTime();
		for( int i = 0; i < 1000000; i++){
			if(i%1000==0)
				System.out.println(i);
			date = date.minusHours(1);
			ODocument event = new ODocument("GaiaEvent");
			event.field("name","Prova").field("timestamp", date.toDate()).field("value",Math.random()).field("class",this.getClass().getName());
			event.save();
		}
		db.close();
	}

	@Test
	public void testRead(){

	}
	@Test
	public void testDateDatime(){
		DateTime date = new DateTime();
		System.err.println(date.getMillis());
		System.err.println(date.minusHours(1).getMillis());
		System.err.println(date.getMillis());
	}

}

