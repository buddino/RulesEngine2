package it.cnit.gaia.rulesengine.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class CompositeDTO {
	@JsonProperty("rules")
	List<RuleDTO> rules;
	@JsonProperty("operator")
	String operator;
	@JsonProperty("name")
	String name;
	@JsonProperty("suggestion")
	String suggestion;
	@JsonProperty("rid")
	@ApiModelProperty(readOnly = true, example = "25:6", value = "Unique identifier of the Rule")
	String rid;

	public List<RuleDTO> getRules() {
		return rules;
	}

	public CompositeDTO setRules(List<RuleDTO> rules) {
		this.rules = rules;
		return this;
	}

	public String getOperator() {
		return operator;
	}

	public CompositeDTO setOperator(String operator) {
		this.operator = operator;
		return this;
	}

	public String getName() {
		return name;
	}

	public CompositeDTO setName(String name) {
		this.name = name;
		return this;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public CompositeDTO setSuggestion(String suggestion) {
		this.suggestion = suggestion;
		return this;
	}

	public String getRid() {
		return rid;
	}

	public CompositeDTO setRid(String rid) {
		this.rid = rid;
		return this;
	}
}
