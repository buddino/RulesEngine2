package it.cnit.gaia.rulesengine.model.event;

import it.cnit.gaia.rulesengine.model.notification.GAIANotification;

import java.util.Date;
import java.util.Map;

public class GaiaEvent {
	Date timestamp;
	String ruleId;
	Map<String, Object> values;

	public GaiaEvent() {
	}

	public GaiaEvent(GAIANotification notification) {
		ruleId = notification.getRule();
		timestamp = new Date(notification.getTimestamp());
		values = notification.getValues();
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public GaiaEvent setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public String getRuleId() {
		return ruleId;
	}

	public GaiaEvent setRuleId(String ruleId) {
		this.ruleId = ruleId;
		return this;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public GaiaEvent setValues(Map<String, Object> values) {
		this.values = values;
		return this;
	}
}
