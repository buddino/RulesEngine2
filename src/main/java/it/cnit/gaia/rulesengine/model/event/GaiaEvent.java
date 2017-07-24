package it.cnit.gaia.rulesengine.model.event;

import it.cnit.gaia.rulesengine.model.notification.GAIANotification;

import java.util.Date;
import java.util.Map;

public class GaiaEvent {
	Date timestamp;
	String rule;
	Long aid;
	Map<String, Object> values;

	public GaiaEvent() {
	}

	public GaiaEvent(GAIANotification notification) {
		rule = notification.getRuleClass();
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

	public String getRule() {
		return rule;
	}

	public GaiaEvent setRule(String rule) {
		this.rule = rule;
		return this;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public GaiaEvent setValues(Map<String, Object> values) {
		this.values = values;
		return this;
	}

	public Long getAid() {
		return aid;
	}

	public GaiaEvent setAid(Long aid) {
		this.aid = aid;
		return this;
	}
}
