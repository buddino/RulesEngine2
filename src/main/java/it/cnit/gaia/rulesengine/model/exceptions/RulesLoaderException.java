package it.cnit.gaia.rulesengine.model.exceptions;

public class RulesLoaderException extends Exception {
	public RulesLoaderException() {
	}

	public RulesLoaderException(String message) {
		super(message);
	}

	public RulesLoaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public RulesLoaderException(Throwable cause) {
		super(cause);
	}

	public RulesLoaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
