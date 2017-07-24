package it.cnit.gaia.rulesengine.model.notification;

import it.cnit.gaia.rulesengine.model.Area;
import it.cnit.gaia.rulesengine.model.School;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class GAIANotification implements Serializable {
	private Long timestamp = new Date().getTime();
	private School school;
	private Area area;

	private String ruleClass = "";
	private String ruleName = "";
	private String ruleId = "";

	private Map values;
	private String description = "";
	private String suggestion = "";
	private NotificationType type = NotificationType.info;


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GAIANotification)) return false;
		GAIANotification that = (GAIANotification) o;
		if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
		if (ruleClass != null ? !ruleClass.equals(that.ruleClass) : that.ruleClass != null) return false;
		if (values != null ? !values.equals(that.values) : that.values != null) return false;
		if (description != null ? !description.equals(that.description) : that.description != null) return false;
		return type == that.type;
	}

	@Override
	public int hashCode() {
		int result = timestamp != null ? timestamp.hashCode() : 0;
		result = 31 * result + (ruleClass != null ? ruleClass.hashCode() : 0);
		result = 31 * result + (values != null ? values.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public GAIANotification setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public String getRuleClass() {
		return ruleClass;
	}

	public GAIANotification setRuleClass(String ruleClass) {
		this.ruleClass = ruleClass;
		return this;
	}

	public Map getValues() {
		return values;
	}

	public GAIANotification setValues(Map values) {
		this.values = values;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public GAIANotification setDescription(String description) {
		this.description = description;
		return this;
	}

	public NotificationType getType() {
		return type;
	}

	public GAIANotification setType(NotificationType type) {
		this.type = type;
		return this;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public GAIANotification setSuggestion(String suggestion) {
		this.suggestion = suggestion;
		return this;
	}

	@Override
	public String toString() {
		return "GAIANotification{" +
				"timestamp=" + timestamp +
				", ruleClass='" + ruleClass + '\'' +
				", values=" + values +
				", description='" + description + '\'' +
				", suggestion='" + suggestion + '\'' +
				", type=" + type +
				'}';
	}

	public String getRuleName() {
		return ruleName;
	}

	public GAIANotification setRuleName(String ruleName) {
		this.ruleName = ruleName;
		return this;
	}

	public School getSchool() {
		return school;
	}

	public GAIANotification setSchool(School school) {
		this.school = school;
		return this;
	}

	public Area getArea() {
		return area;
	}

	public GAIANotification setArea(Area area) {
		this.area = area;
		return this;
	}

	public String getRuleId() {
		return ruleId;
	}

	public GAIANotification setRuleId(String ruleId) {
		this.ruleId = ruleId;
		return this;
	}

}
