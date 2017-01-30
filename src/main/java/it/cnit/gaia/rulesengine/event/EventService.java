package it.cnit.gaia.rulesengine.event;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.event.GaiaEvent;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

	@Autowired
	OrientGraphFactory graphFactory;
	private Logger LOGGER = Logger.getLogger(this.getClass().getSimpleName());
	String fetchplan = "rule:1";


	public void addEvent(GaiaEvent event) {
		//Get the underlying Document database ( GaiaEvent is not a subclass of the Vertex class)
		ODatabaseDocumentTx rawGraph = graphFactory.getTx().getRawGraph();
		ODocument gaiaevent = new ODocument("GaiaEvent");
		//Fill the fields
		gaiaevent.field("timestamp", event.getTimestamp());
		gaiaevent.field("rule", event.getRuleId());
		gaiaevent.field("values", event.getValues());
		//Save the document, it works as long as an active database is in the scope
		gaiaevent.save();
		rawGraph.commit().close();
		LOGGER.debug("\u001B[36mNEW EVENT\u001B[0m\t");
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
	public List<ODocument> getLatestEvents(int limit, boolean prefetch) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("SELECT * FROM GaiaEvent order by timestamp DESC");
		if (prefetch)
			query.setFetchPlan(fetchplan);
		query.setLimit(limit);
		return db.query(query);
	}

	public List<ODocument> getEventsForRule(String ruleId, int limit, boolean prefetch) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("SELECT * FROM GaiaEvent WHERE rule=? order by timestamp DESC");
		query.setLimit(limit);
		if (prefetch)
			query.setFetchPlan(fetchplan);
		List<ODocument> result = db.command(query).execute(ruleId);
		return result;

	}

	public List<ODocument> getEventsForRule(GaiaRule rule, int limit, boolean prefetch) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
				OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("SELECT * FROM GaiaEvent WHERE rule = ? order by timestamp DESC");
		query.setLimit(limit);
		if (prefetch)
			query.setFetchPlan(fetchplan);
		List<ODocument> result = db.command(query).execute(rule.getRid());
		return result;
	}
	public List<ODocument> getEventsByRuleClass(String ruleClass, int limit, boolean prefetch) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("SELECT * FROM GaiaEvent WHERE rule.@class = ? ORDER BY timestamp DESC");
		query.setLimit(limit);
		if (prefetch)
			query.setFetchPlan(fetchplan);
		List<ODocument> result = db.command(query).execute(ruleClass);
		return result;
	}




}
