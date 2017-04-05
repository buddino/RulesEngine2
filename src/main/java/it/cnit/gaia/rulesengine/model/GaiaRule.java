package it.cnit.gaia.rulesengine.model;

import com.google.gson.Gson;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.configuration.ContextProvider;
import it.cnit.gaia.rulesengine.event.EventService;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.model.event.GaiaEvent;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import it.cnit.gaia.rulesengine.notification.WebsocketService;
import it.cnit.gaia.rulesengine.rules.ExpressionRule;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class GaiaRule implements Fireable {

	//Riguarda Mixing a lot of responsabilities including Queries to the DB

	@LoadMe
	@LogMe(event = false)
	public String name;
	@LoadMe
	public String suggestion;
	@LogMe(event = false, notification = false)
	public String description;

	public String rid;
	public School school;

	//TODO Add minimum interval between triggering
	@LoadMe(required = false)
	public Long intervalInSeconds = 0L;
	/*
	Ask the database last time the rule has been triggered
	Compute the difference
	Comprare the difference with the minimum period of the rule
	If equals to 0 or not present do not query the DB
	* */

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	protected WebsocketService websocket = ContextProvider.getBean(WebsocketService.class);
	protected EventService eventService = ContextProvider.getBean(EventService.class);
	protected MeasurementRepository measurements = ContextProvider.getBean(MeasurementRepository.class);
	protected OrientGraphFactory graphFactory = ContextProvider.getBean(OrientGraphFactory.class);

	public abstract boolean condition();

	public void action() {
		GAIANotification notification = getBaseNotification();
		GaiaEvent event = getBaseEvent();
		setLatestTrigger();
		websocket.pushNotification(notification);
		ODocument evt = eventService.addEvent(event);
	}

	protected void setLatestTrigger(){
		OrientVertex vertex = graphFactory.getNoTx().getVertex(rid);
		vertex.setProperty("latest_event", new Date());
		vertex.save();
	}

	private boolean isTriggeringIntervalValid(){
		if(intervalInSeconds == 0)
			return true;
		Date latest = getLatestEventTimestamp();
		if( latest == null)
			return true;
		Date now = new Date();
		return now.getTime() - latest.getTime() > intervalInSeconds * 1000;
	}

	public void fire() {
		if(!isTriggeringIntervalValid()) {
		LOGGER.debug(String.format("Rule %s not triggered beacuse of the interval contraint", rid));
				return;
		}
		try {
			if (condition())
				action();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	//TODO Excpetion
	protected boolean validateFields() {
		Field[] fields = this.getClass().getFields();
		for (Field f : fields) {
			if (f.isAnnotationPresent(LoadMe.class)) {
				LoadMe annotation = f.getAnnotation(LoadMe.class);
				if (annotation.required()) {
					try {
						if (f.get(this) == null || f.get(this).equals("")) {
							return false;
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return true;
	}

	public boolean init() throws RuleInitializationException {
		return validateFields();
	}

	protected Set<String> getURIs() {
		Field[] fields = this.getClass().getFields();
		Set<String> uriSet = new HashSet<>();
		for (Field f : fields) {
			if (f.isAnnotationPresent(URI.class)) {
				try {
					String uri = (String) f.get(this);
					uriSet.add(uri);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return uriSet;
	}

	protected GAIANotification getBaseNotification() {
		GAIANotification notification = new GAIANotification();
		//TODO Area
		notification.setRuleClass(this.getClass().getSimpleName())
				.setRuleName(name)
				.setRuleId(rid)
				.setDescription(description)
				.setSuggestion(getSuggestion())
				.setSchool(school)
				.setValues(getFieldsForNotification());
		return notification;
	}

	protected GaiaEvent getBaseEvent() {
		GaiaEvent event = new GaiaEvent();
		Map<String, Object> fieldsForEvent = getFieldsForEvent();
		fieldsForEvent.put("suggestion", getSuggestion());
		event.setTimestamp(new Date()).setRuleId(rid).setValues(fieldsForEvent);
		return event;
	}

	protected Map<String, Object> getFieldsForNotification() {
		Field[] fields = this.getClass().getFields();
		Map<String, Object> map = new HashMap<>();
		for (Field f : fields) {
			if (f.isAnnotationPresent(LogMe.class)) {
				if (f.getAnnotation(LogMe.class).notification()) {
					try {
						Object value = f.get(this);
						String fieldName = f.getName();
						map.put(fieldName, value);

					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return map;
	}

	protected Map<String, Object> getFieldsForEvent() {
		Field[] fields = this.getClass().getFields();
		Map<String, Object> map = new HashMap<>();
		for (Field f : fields) {
			if (f.isAnnotationPresent(LogMe.class)) {
				if (f.getAnnotation(LogMe.class).event()) {
					try {
						Object value = f.get(this);
						String fieldName = f.getName();
						map.put(fieldName, value);

					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return map;
	}

	protected Map<String, Object> getAllFields() {
		//TODO Remove Gson here
		Gson G = new Gson();
		Map<String, Object> map = getFieldsForEvent();
		map.putAll(getFieldsForNotification());
		//Riguarda It there a better way?
		if (this instanceof ExpressionRule) {
			map.putAll(((ExpressionRule) this).fields);
		}
		return map;
	}

	public String getName() {
		return name;
	}

	public GaiaRule setName(String name) {
		this.name = name;
		return this;
	}

	public String getSuggestion() {
		Map<String, Object> fields = getAllFields();
		fields.put("school", school.getName());

		//Add useful fields
		//...
		String replaced = StrSubstitutor.replace(suggestion, fields);
		return replaced;
	}

	public String getDescription() {
		return description;
	}

	public GaiaRule setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getRid() {
		return rid;
	}

	public School getSchool() {
		return school;
	}

	public GaiaRule setSchool(School school) {
		this.school = school;
		return this;
	}

	public String getPath() {
		//Riguarda Maybe a static method?
		OrientGraphNoTx noTx = graphFactory.getNoTx();
		ORID identity = noTx.getVertex(rid).getIdentity();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select unionall(name) as path from (traverse in() from ?)");
		List<ODocument> execute = query.execute(identity);
		List<String> path = execute.get(0).field("path");
		Collections.reverse(path);
		String uri = path.stream().collect(Collectors.joining("/"));
		return uri;
	}

	public Date getLatestEventTimestamp() {
		ODocument latestEvent = getLatestEvent();
		if (latestEvent == null)
			return null;
		return (Date) latestEvent.field("timestamp");
	}

	public ODocument getLatestEvent() {
		OrientGraphNoTx G = graphFactory.getNoTx();
		OSQLSynchQuery query = new OSQLSynchQuery("select from GaiaEvent where rule=? ORDER BY timestamp DESC LIMIT 1");
		List<ODocument> result = (List<ODocument>) query.execute(G.getVertex(rid).getIdentity());
		if (result.size() == 0 || result == null) {
			return null;
		}
		return result.get(0);
	}

}
