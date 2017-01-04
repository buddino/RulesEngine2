package it.cnit.gaia.rulesengine.model.notification;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class GAIANotification implements Serializable {

    private Long timestamp = new Date().getTime();
    private String rule;
    private Map values;
    private String description;
    private String suggestion;
    private NotificationType type = NotificationType.info;




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GAIANotification)) return false;
        GAIANotification that = (GAIANotification) o;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
        if (rule != null ? !rule.equals(that.rule) : that.rule != null) return false;
        if (values != null ? !values.equals(that.values) : that.values != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = timestamp != null ? timestamp.hashCode() : 0;
        result = 31 * result + (rule != null ? rule.hashCode() : 0);
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

    public String getRule() {
        return rule;
    }

    public GAIANotification setRule(String rule) {
        this.rule = rule;
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
                ", rule='" + rule + '\'' +
                ", values=" + values +
                ", description='" + description + '\'' +
                ", suggestion='" + suggestion + '\'' +
                ", type=" + type +
                '}';
    }
}
