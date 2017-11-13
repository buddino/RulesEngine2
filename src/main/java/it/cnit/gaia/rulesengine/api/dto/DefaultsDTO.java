package it.cnit.gaia.rulesengine.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

public class DefaultsDTO {

	@JsonProperty("fields")
	@ApiModelProperty(value = "A json object containing the default values for the fields of the rule. {fieldname : {value:...,description:...,required:true|false}")
	private Map<String, Map<String, Object>> fields;

	@JsonProperty("suggestion")
	@ApiModelProperty(value = "A json object containing the suggestion of the rule in the form {\"it\":\"italian suggestion\",\"en\":\"english suggeston\"}")
	private Map<String, String> suggestion;


	public Map<String, Map<String, Object>> getFields() {
		return fields;
	}

	public DefaultsDTO setFields(Map<String, Map<String, Object>> fields) {
		this.fields = fields;
		return this;
	}

	public Map<String, String> getSuggestion() {
		return suggestion;
	}

	public DefaultsDTO setSuggestion(Map<String, String> suggestion) {
		this.suggestion = suggestion;
		return this;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("DefaultsDTO{");
		sb.append(", fields=").append(fields);
		sb.append(", suggestion=").append(suggestion);
		sb.append('}');
		return sb.toString();
	}
}
