package it.cnit.gaia.rulesengine.event;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class EventService {

	@Autowired
	OPartitionedDatabasePool eventdbpool;

	public void addEvent() {
		ODatabaseDocumentTx db = eventdbpool.acquire();
		//TODO
		try {
			ODocument event = new ODocument("GaiaEvent");
			event.save();
		}
		finally {
			db.close();
		}
	}

	public void timerange(Date from, Date to){
		ODatabaseDocumentTx db = eventdbpool.acquire();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from Profile where name = ? and surname = ?");
		List<ODocument> result = db.command(query).execute(new Date(1451602800000L), new Date(1484002800000L));
		System.out.println(result.toString());

	}



}
