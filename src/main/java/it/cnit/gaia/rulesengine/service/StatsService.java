package it.cnit.gaia.rulesengine.service;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsService {

	@Autowired
	OrientGraphFactory graphFactory;
	@Autowired
	RulesLoader rulesLoader;


	public Long getEventsCount(Long from, Long to) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select count(@rid) from GaiaEvent where timestamp between ? and ?");
		List result = db.command(query).execute(from, to);
		ODocument o = (ODocument) result.get(0);
		return o.field("count");
	}

	public Long getEventsCount(Long from, Long to, String classname) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select count(@rid) from GaiaEvent where timestamp between ? and ? and rule.@class = ?");
		List result = db.command(query).execute(from, to, classname);
		ODocument o = (ODocument) result.get(0);
		return o.field("count");
	}

	public List<ODocument> getEventsForeachRuleClass(Long from, Long to) {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select rule.@class as ruleClass, count(@rid)from GaiaEvent where timestamp between ? and ? group by rule.@class");
		List<ODocument> result = db.command(query).execute(from, to);
		return result;
	}

	public List<ODocument> getCountLatestNDays(Long days) {
		LocalDateTime localDateTime = LocalDateTime.now();
		long beginning = localDateTime.minusDays(days).toInstant(ZoneOffset.UTC).toEpochMilli();
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
				"select ts, count(*) as events from (select timestamp.format('dd-MM-yyyy') " +
						"as ts from GaiaEvent where timestamp > ? order by timestamp) " +
						"group by ts");
		List<ODocument> result = db.command(query).execute(beginning);
		List<ODocument> sorted = result.stream().sorted((o, p) -> compareDate(o.field("ts"), p.field("ts")))
									   .collect(Collectors.toList());
		return sorted;
	}

	public List<ODocument> getCountLatestNDaysForClass(Long days, String ruleClass) {
		LocalDateTime localDateTime = LocalDateTime.now();
		long beginning = localDateTime.minusDays(days).toInstant(ZoneOffset.UTC).toEpochMilli();
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
				"select ts, count(*) as events from (select timestamp.format('dd-MM-yyyy') " +
						"as ts from GaiaEvent where timestamp > ? and rule.@class = ? order by timestamp) " +
						"group by ts");
		List<ODocument> result = db.command(query).execute(beginning, ruleClass);
		List<ODocument> sorted = result.stream().sorted((o, p) -> compareDate(o.field("ts"), p.field("ts")))
									   .collect(Collectors.toList());
		return sorted;
	}

	public Long getCountActiveRuleInstances() {
		if (rulesLoader.getRuleMap() == null)
			return 0L;
		return Long.valueOf(rulesLoader.getRuleMap().size());
	}

	public Long getCountRuleInstanceInDb() {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
				"select count(@rid) from GaiaRule");
		List<ODocument> result = db.command(query).execute();
		ODocument o =  result.get(0);
		return o.field("count");
	}

	public List<ODocument> getLatestFiredRules() {
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
				"select name, @class, latestFireTime, school.name as school from GaiaRule order by latestFireTime desc");
		query.setLimit(30);
		List<ODocument> result = db.command(query).execute();
		return result;
	}

	public List<ODocument> getRulesGroupedByClass(){
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
				"select @class, count(*) from GaiaRule group by @class");
		query.setLimit(10);
		List<ODocument> result = db.command(query).execute();
		return result;
	}

	public List<ODocument> getEventsGroupedBySchool(){
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
				"select rule.school.name as school, count(*) from GaiaEvent group by rule.school");
		List<ODocument> result = db.command(query).execute();
		return result;
	}

	public Long getDisabledRules(){
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
				"select count(@rid) from GaiaRule where enabled=false");
		List<ODocument> result = db.command(query).execute();
		ODocument o =  result.get(0);
		return o.field("count");
	}

	public Long getCustomRules(){
		ODatabaseDocumentTx db = graphFactory.getNoTx().getRawGraph();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
				"select count(@rid) from GaiaRule where custom=true");
		List<ODocument> result = db.command(query).execute();
		ODocument o =  result.get(0);
		return o.field("count");
	}



	/**
	 * Compares 2 strings encoding a date using in the format dd-MM-yyyy
	 *
	 * @param d1 First string
	 * @param d2 Second string
	 * @return
	 */
	private int compareDate(String d1, String d2) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		try {
			Date l1 = sdf.parse(d1);
			Date l2 = sdf.parse(d2);
			return l1.compareTo(l2);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}


}
