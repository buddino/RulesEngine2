package it.cnit.gaia.rulesengine.model;

import it.cnit.gaia.rulesengine.configuration.ContextProvider;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.notification.WebSocketController;
import org.mapdb.DB;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class GaiaRule implements Fireable {
	//TODO Move something here?

	@ToBeLogged
	public String name;
	public String suggestion;
	public String description;

	protected DB embeddedDB = ContextProvider.getBean(DB.class);
	//protected SenderService amqpSenderService = ContextProvider.getBean(SenderService.class);
	protected WebSocketController websocket = ContextProvider.getBean(WebSocketController.class);
	//protected EventLogger eventLogger = ContextProvider.getBean(EventLogger.class);
	protected MeasurementRepository measurements = ContextProvider.getBean(MeasurementRepository.class);

	public  abstract boolean condition();
	public abstract void action();
	public void fire(){
		if(condition())
			action();
	}

	protected Map<String,Object> getFieldMap(){

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
}
