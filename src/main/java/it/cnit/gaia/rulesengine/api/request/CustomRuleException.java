package it.cnit.gaia.rulesengine.api.request;

public class CustomRuleException extends Exception {
	public CustomRuleException() {
		super();
	}

	public CustomRuleException(String message) {
		super(message);
	}

	public CustomRuleException(String message, Throwable cause) {
		super(message, cause);
	}

	public CustomRuleException(Throwable cause) {
		super(cause);
	}
}
