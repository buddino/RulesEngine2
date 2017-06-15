package it.cnit.gaia.rulesengine.api.request;

import java.util.Date;
import java.util.Map;

public class EventDTO {
	Date timestamp;
	Map<String,Object> values;
	RuleDTO rule;

	public Date getTimestamp() {
		return timestamp;
	}

	public EventDTO setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public Map<String, Object> getValues() {
		return values;
	}

	public EventDTO setValues(Map<String, Object> values) {
		this.values = values;
		return this;
	}

	public RuleDTO getRule() {
		return rule;
	}

	public EventDTO setRule(RuleDTO rule) {
		this.rule = rule;
		return this;
	}
}
