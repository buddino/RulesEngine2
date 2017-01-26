package it.cnit.gaia.rulesengine.event;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.event.GaiaEvent;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {

	@Autowired
	OrientGraphFactory graphFactory;
	private Logger LOGGER = Logger.getLogger(this.getClass().getSimpleName());


	public void addEvent(GaiaEvent event) {
		//Get the underlying Document database ( GaiaEvent is not a subclass of the Vertex class)
		ODatabaseDocumentTx rawGraph = graphFactory.getTx().getRawGraph();
		ODocument gaiaevent = new ODocument("GaiaEvent");
		//Fill the fields
		gaiaevent.field("timestamp", event.getTimestamp());
		gaiaevent.field("rule", event.getRuleId());
		gaiaevent.field("ruleName", event.getRuleName());
		gaiaevent.field("values", event.getValues());
		//Save the document, it works as long as an active database is in the scope
		gaiaevent.save();
		rawGraph.commit().close();
		LOGGER.debug("Added event");
	}

	public void addEvent(GAIANotification notification) {
		GaiaEvent gaiaEvent = new GaiaEvent(notification);
		addEvent(gaiaEvent);
	}

	/**
	 * @param limit    the number of object to be returned
	 * @param prefetch if true the rule field will be fetched
	 * @return Iterable of OrientVertex. The record (ODocument) can be retrieved using .getRecord()
	 */
	public Iterable<OrientVertex> getLatestEvents(int limit, boolean prefetch) {
		OrientGraphNoTx graph = graphFactory.getNoTx();
		OCommandSQL command = new OCommandSQL("SELECT * FROM GaiaEvent order by timestamp DESC");
		if (prefetch)
			command.setFetchPlan("*:-1");
		command.setLimit(limit);
		return graph.command(command).execute();
	}

	public Iterable<OrientVertex> getEventsForRule(String ruleId, int limit) {
		OrientGraphNoTx graph = graphFactory.getNoTx();
		OCommandSQL command = new OCommandSQL("SELECT * FROM GaiaEvent where rule=?order by timestamp DESC");
		command.setLimit(limit);
		return graph.command(command).execute(ruleId);
	}

	public Iterable<OrientVertex> getEventsForRule(GaiaRule rule, int limit) {
		OrientGraphNoTx graph = graphFactory.getNoTx();
		OCommandSQL command = new OCommandSQL("SELECT * FROM GaiaEvent where rule=?order by timestamp DESC");
		command.setLimit(limit);
		return graph.command(command).execute(rule.getRid());
	}

	//TODO Ruleclass



}
