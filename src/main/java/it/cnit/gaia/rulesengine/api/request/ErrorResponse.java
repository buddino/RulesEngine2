package it.cnit.gaia.rulesengine.api.request;

public class ErrorResponse {
	Integer code;
	String message;

	public ErrorResponse(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public ErrorResponse setCode(Integer code) {
		this.code = code;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public ErrorResponse setMessage(String message) {
		this.message = message;
		return this;
	}
}
