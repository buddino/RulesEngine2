package it.cnit.gaia.rulesengine.model;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import it.cnit.gaia.rulesengine.configuration.ContextProvider;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.annotation.ToBeLogged;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import it.cnit.gaia.rulesengine.model.notification.NotificationType;
import it.cnit.gaia.rulesengine.notification.WebSocketController;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class GaiaRule implements Fireable {
	@ToBeLogged
	public String name;
	public String suggestion;
	public String description;
	public String rid;
	protected Logger LOGGER = Logger.getLogger(this.getClass());
	//protected DB embeddedDB = ContextProvider.getBean(DB.class);
	//protected SenderService amqpSenderService = ContextProvider.getBean(SenderService.class);
	protected WebSocketController websocket = ContextProvider.getBean(WebSocketController.class);
	//protected EventLogger eventLogger = ContextProvider.getBean(EventLogger.class);
	protected MeasurementRepository measurements = ContextProvider.getBean(MeasurementRepository.class);
	protected OrientGraphFactory graphFactory = ContextProvider.getBean(OrientGraphFactory.class);

	public abstract boolean condition();
	public void action(){
		GAIANotification notification = getBaseNotification();
		websocket.pushNotification(notification);
		System.out.println("\u001B[36mNOTIFICATION\u001B[0m\t"+notification.toString());
	}
	public void fire(){
		if(condition())
			action();
	}

	//TODO Remove when implemented by rules
	public boolean init(){
		return true;
	}

	protected Set<String> getURIs(){
		Field[] fields = this.getClass().getFields();
		Set<String> uriSet = new HashSet<>();
		for(Field f : fields) {
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

	protected GAIANotification getBaseNotification(){
		GAIANotification notification = new GAIANotification();
		notification.setRule(this.getClass().getSimpleName())
				.setName(name)
				.setType(NotificationType.info)
				.setDescription(description)
				.setSuggestion(suggestion)
				.setValues(getToBeLoggedMap());
		return notification;
	}

	protected Map<String,Object> getToBeLoggedMap(){
		Field[] fields = this.getClass().getFields();
		Map<String, Object> map = new HashMap<>();
		for(Field f : fields) {
			if (f.isAnnotationPresent(ToBeLogged.class)) {
				try {
					Object value = f.get(this);
					String fieldName = f.getName();
					map.put(fieldName, value);

				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
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
		return suggestion;
	}

	public GaiaRule setSuggestion(String suggestion) {
		this.suggestion = suggestion;
		return this;
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
	// TODO Update method for resources URIs injection
	// ${name}_uri or annotation

}
