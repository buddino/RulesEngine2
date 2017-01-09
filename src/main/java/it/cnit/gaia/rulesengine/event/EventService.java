package it.cnit.gaia.rulesengine.event;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


}
