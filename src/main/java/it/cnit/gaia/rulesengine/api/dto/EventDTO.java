package it.cnit.gaia.rulesengine.api.dto;

import java.util.Date;
import java.util.Map;

public class EventDTO {
	String rid;
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

	public String getRid() {
		return rid;
	}

	public EventDTO setRid(String rid) {
		this.rid = rid;
		return this;
	}
}
