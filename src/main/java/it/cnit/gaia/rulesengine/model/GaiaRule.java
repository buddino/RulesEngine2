package it.cnit.gaia.rulesengine.model;

import com.google.gson.Gson;
import io.swagger.client.ApiException;
import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.rulesengine.configuration.ContextProvider;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.model.event.GaiaEvent;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import it.cnit.gaia.rulesengine.rules.ExpressionRule;
import it.cnit.gaia.rulesengine.service.*;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

public abstract class GaiaRule implements Fireable {

	//Riguarda Questa classe ha troppe responsabilità

	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	/**
	 * The name of the rule, mostly useful for heavily customized instances.
	 * It is not required meaning a rule with no name can be instantiated
	 */
	@LoadMe(required = false)
	@LogMe(event = false)
	public String name;

	/**
	 * The suggestion to sent to the client
	 */
	@LoadMe(required = false)
	public String suggestion;

	@LogMe(event = false, notification = false)
	public String description;

	/**
	 * The id of the rule in the persistency layer, filled during instantiation
	 */
	public String rid;
	/**
	 * The School object associated with the rule, linked during loading
	 */
	public School school;
	public Area area;

	/**
	 * Interval in seconds between 2 fires (e.g. 900 means at most one time every 15 minutes)
	 */
	@LoadMe(required = false)
	public Long intervalInSeconds = 0L;

	/**
	 * The timestamp of the latest fire
	 */
	public Date latestFireTime;

	/**
	 * Services, manually loaded from the application context
	 */
	protected WebsocketService websocket = ContextProvider.getBean(WebsocketService.class);
	protected EventService eventService = ContextProvider.getBean(EventService.class);
	protected MeasurementRepository measurements = ContextProvider.getBean(MeasurementRepository.class);
	protected BuildingDatabaseService buildingDBService = ContextProvider.getBean(BuildingDatabaseService.class);
	protected RuleDatabaseService ruleDatabaseService = ContextProvider.getBean(RuleDatabaseService.class);
	protected ScheduleService scheduleService = ContextProvider.getBean(ScheduleService.class);

	/**
	 * Default fire() behavior is if( condition() ) then action()
	 * The condition() method is abstract and must be specified in the inheriting subclass
	 */
	public abstract boolean condition();

	/**
	 * The default behavior of the action() method is log the event using the event service and to send a notification through the websocket channel
	 */
	public void action() {
		GAIANotification notification = getBaseNotification();
		GaiaEvent event = getBaseEvent();
		websocket.pushNotification(notification);
		eventService.addEvent(event);
	}

	/**
	 * Checks if the rule can be fired
	 *
	 * @return true if the rule can be fired or false if it is not the time yet
	 */
	private boolean isTriggeringIntervalValid() {
		if (intervalInSeconds == 0)        //There rule will be fires at every iteration
			return true;
		Date now = new Date();
		if (latestFireTime == null)        //The rule has never been fired before
			return true;                    //return TRUE to fire
		return now.getTime() - latestFireTime
				.getTime() > intervalInSeconds * 1000;
	}

	/**
	 * The default implementation of the fire method is to check if the right amount of time passed
	 */
	public void fire() {
		if (!isTriggeringIntervalValid()) {
			LOGGER.debug(String.format("Rule %s not triggered beacuse of the interval constraint", rid));
			return;
		}
		//Update the latest fire time. This is kept in memory only.
		//In case of restart all the rules are fired again
		latestFireTime = new Date();
		try {
			if (condition()) {
				action();
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	protected boolean validateFields() throws RuleInitializationException {
		Field[] fields = this.getClass().getFields();
		for (Field f : fields) {
			if (f.isAnnotationPresent(LoadMe.class)) {
				LoadMe annotation = f.getAnnotation(LoadMe.class);
				if (annotation.required()) {
					try {
						if (f.get(this) == null || f.get(this).equals("")) {
							throw new RuleInitializationException(String
									.format("Required field missing or empty (%s)", f.getName()));
						}
						if (f.isAnnotationPresent(URI.class)) {
							//FIXME Checking by querying here, results in a double query and in a slow validation time
							//Potrebbe essere un'idea fare il mapping non tutto insieme ma via via che si aggiungono uri (perdo parallel)
							measurements.checkUri( (String) f.get(this) );
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (ApiException e) {
						throw new RuleInitializationException(String
								.format("Required uri not found in resource map (%s)", f.getName()));
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
		event.setTimestamp(new Date()).setRule(rid).setValues(fieldsForEvent);
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

	public Long getParentAreaId() {
		return ruleDatabaseService.getParentArea(rid);
	}


	// SETTER FOR SERVICES //
	public GaiaRule setWebsocket(WebsocketService websocket) {
		this.websocket = websocket;
		return this;
	}

	public GaiaRule setEventService(EventService eventService) {
		this.eventService = eventService;
		return this;
	}

	public GaiaRule setMeasurements(MeasurementRepository measurements) {
		this.measurements = measurements;
		return this;
	}

	public GaiaRule setBuildingDBService(BuildingDatabaseService buildingDBService) {
		this.buildingDBService = buildingDBService;
		return this;
	}

	public GaiaRule setRuleDatabaseService(RuleDatabaseService ruleDatabaseService) {
		this.ruleDatabaseService = ruleDatabaseService;
		return this;
	}

	public GaiaRule setScheduleService(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
		return this;
	}
}
