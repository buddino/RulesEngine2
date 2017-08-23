package it.cnit.gaia.rulesengine.model.exceptions;

public class RuleInitializationException extends Exception {
	private String rule;

	public RuleInitializationException() {
	}

	public RuleInitializationException(String message) {
		super(message);
	}

	public RuleInitializationException(String message, String rule) {
		super(message);
		this.rule = rule;
	}

	public RuleInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public RuleInitializationException(Throwable cause) {
		super(cause);
	}

	public RuleInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public String getRule() {
		return rule;
	}
}
