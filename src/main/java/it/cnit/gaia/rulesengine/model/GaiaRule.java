package it.cnit.gaia.rulesengine.model;

import com.google.gson.Gson;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import it.cnit.gaia.rulesengine.configuration.ContextProvider;
import it.cnit.gaia.rulesengine.event.EventService;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.model.event.GaiaEvent;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import it.cnit.gaia.rulesengine.notification.WebsocketService;
import it.cnit.gaia.rulesengine.rules.ExpressionRule;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class GaiaRule implements Fireable {
	@LoadMe
	@LogMe(event = false)
	public String name;
	@LoadMe
	public String suggestion;
	@LogMe(event = false, notification = false)
	public String description;

	public String rid;
	public School school;

	protected Logger LOGGER = Logger.getLogger(this.getClass());

	protected WebsocketService websocket = ContextProvider.getBean(WebsocketService.class);
	protected EventService eventService = ContextProvider.getBean(EventService.class);
	protected MeasurementRepository measurements = ContextProvider.getBean(MeasurementRepository.class);
	protected OrientGraphFactory graphFactory = ContextProvider.getBean(OrientGraphFactory.class);

	public abstract boolean condition();

	public void action() {
		GAIANotification notification = getBaseNotification();
		GaiaEvent event = getBaseEvent();
		websocket.pushNotification(notification);
		eventService.addEvent(event);
	}

	public void fire() {
		try {
			if (condition())
				action();
		}
		catch (Exception e){
			LOGGER.error(e.getMessage());
		}
	}

	public boolean validateFields() {
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

	public boolean init() throws Exception {
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
		fieldsForEvent.put("suggestion",getSuggestion());
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
		if( this instanceof ExpressionRule ){
			map.putAll( ((ExpressionRule)this).fields );
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

	public String getPath(){
		OrientGraphNoTx noTx = graphFactory.getNoTx();
		ORID identity = noTx.getVertex("#21:17121").getIdentity();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select unionall(name) as path from (traverse in() from ?)");
		List<ODocument> execute = query.execute(identity);
		List<String> path = execute.get(0).field("path");
		Collections.reverse(path);
		String uri = path.stream().collect(Collectors.joining("/"));
		return uri;
	}
}
