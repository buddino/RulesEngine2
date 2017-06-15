package it.cnit.gaia.rulesengine.api.request;

import org.springframework.http.HttpStatus;

public class GaiaRuleException extends Exception {
	private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

	public GaiaRuleException() {
		super();
	}

	public GaiaRuleException(String message) {
		super(message);
	}

	public GaiaRuleException(String message, int status) {
		super(message);
		this.status =HttpStatus.valueOf(status);
	}

	public GaiaRuleException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public GaiaRuleException(String message, Throwable cause) {
		super(message, cause);
	}

	public GaiaRuleException(Throwable cause) {
		super(cause);
	}

	public HttpStatus getStatus() {
		return status;
	}

	public GaiaRuleException setStatus(HttpStatus status) {
		this.status = status;
		return this;
	}
}
