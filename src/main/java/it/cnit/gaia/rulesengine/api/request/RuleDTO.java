package it.cnit.gaia.rulesengine.api.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

public class RuleDTO {

	@JsonProperty("rid")
	@ApiModelProperty(readOnly = true, example = "25:6", value = "Unique identifier of the Rule")
	private String rid;
	@JsonProperty("class")
	@ApiModelProperty(example = "CustomThresholdRule",value = "Must be the name of the class of one of the implemented rules, see documentation", required = true)
	private String clazz;
	@JsonProperty("fields")
	@ApiModelProperty(value = "A key:value json object containing the settings of the rule. Some fields are mandatory dpeending on the rule class, see rule documentation.", required = true)
	private Map<String,Object> fields;

	public String getRid() {
		return rid;
	}

	public RuleDTO setRid(String rid) {
		this.rid = rid;
		return this;
	}

	public String getClazz() {
		return clazz;
	}

	public RuleDTO setClazz(String clazz) {
		this.clazz = clazz;
		return this;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public RuleDTO setFields(Map<String, Object> fields) {
		this.fields = fields;
		return this;
	}
}
