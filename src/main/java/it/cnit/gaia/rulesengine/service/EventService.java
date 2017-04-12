package it.cnit.gaia.rulesengine.service;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.event.GaiaEvent;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class EventService {

	@Autowired
	OrientGraphFactory graphFactory;
	private Logger LOGGER = Logger.getLogger(this.getClass().getSimpleName());
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public ODocument addEvent(GaiaEvent event) {
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
		return gaiaevent;
		//LOGGER.debug("\u001B[36mNEW EVENT\u001B[0m\t");
	}

	public ODocument addEvent(GAIANotification notification) {
		GaiaEvent gaiaEvent = new GaiaEvent(notification);
		return addEvent(gaiaEvent);
	}

	/**
	 * @param limit the number of object to be returned
	 * @return Iterable of OrientVertex. The record (ODocument) can be retrieved using .getRecord()
	 */
	public List<ODocument> getLatestEvents(int limit) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("SELECT * FROM GaiaEvent order by timestamp DESC FETCHPLAN rule:1");
		query.setLimit(limit);
		return db.query(query);
	}

	public List<ODocument> getEventsForRule(String ruleId, int limit) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("SELECT * FROM GaiaEvent WHERE rule=? order by timestamp DESC FETCHPLAN rule:1");
		query.setLimit(limit);
		List<ODocument> result = db.command(query).execute(ruleId);
		return result;

	}

	public List<ODocument> getEventsForRule(GaiaRule rule, int limit) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("SELECT * FROM GaiaEvent WHERE rule = ? order by timestamp DESC FETCHPLAN rule:1");
		query.setLimit(limit);
		List<ODocument> result = db.command(query).execute(rule.getRid());
		return result;
	}

	public List<ODocument> getEventsByRuleClass(String ruleClass, int limit) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("SELECT * FROM GaiaEvent WHERE rule.@class = ? ORDER BY timestamp DESC FETCHPLAN rule:1");
		query.setLimit(limit);
		List<ODocument> result = db.command(query).execute(ruleClass);
		return result;
	}

	public List<ODocument> getEventsForSchool(String schoolId, Integer limit) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("SELECT * FROM GaiaEvent WHERE rule.school.id = ? ORDER BY timestamp DESC FETCHPLAN rule:1");
		query.setLimit(limit);
		List<ODocument> result = db.command(query).execute(schoolId);
		return result;
	}

	public List<ODocument> getEventsForSchoolTimeRange(String schoolId, Long from, Long to, Integer limit) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from GaiaEvent where rule.school.id = ? AND timestamp between ? and ? order by timestamp DESC FETCHPLAN rule:1");
		query.setLimit(limit);
		List<ODocument> result = db.command(query).execute(schoolId, sdf.format(from), sdf.format(to));
		return result;
	}

	public List<ODocument> getEventsForSchoolTimeRange(String schoolId, String from, String to, Integer limit) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select from GaiaEvent where rule.school.id = ? AND timestamp between ? and ? order by timestamp DESC FETCHPLAN rule:1");
		query.setLimit(limit);
		List<ODocument> result = db.command(query).execute(schoolId, from, to);
		return result;
	}

	public ODocument getLatestEventForRule(String rid){
		OrientGraphNoTx G = graphFactory.getNoTx();
		OSQLSynchQuery query = new OSQLSynchQuery("select from GaiaEvent where rule=? ORDER BY timestamp DESC LIMIT 1");
		List<ODocument> result = (List<ODocument>) query.execute(G.getVertex(rid).getIdentity());
		if (result.size() == 0 || result == null) {
			return null;
		}
		return result.get(0);
	}

	public Date getLatestEventTimestamp(String rid) {
		ODocument latestEvent = getLatestEventForRule(rid);
		if (latestEvent == null)
			return null;
		return (Date) latestEvent.field("timestamp");
	}
}
