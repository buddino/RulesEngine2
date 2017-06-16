package it.cnit.gaia.rulesengine.api.dto;

public class ConditionDTO {
	private RuleDTO rule;
	private Boolean condition;

	public RuleDTO getRule() {
		return rule;
	}

	public ConditionDTO setRule(RuleDTO rule) {
		this.rule = rule;
		return this;
	}

	public Boolean getCondition() {
		return condition;
	}

	public ConditionDTO setCondition(Boolean condition) {
		this.condition = condition;
		return this;
	}
}
