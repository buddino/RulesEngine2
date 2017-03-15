package it.cnit.gaia.rulesengine.buildingdb;

public class BDBException extends Exception{
	public BDBException() {
	}

	public BDBException(String message) {
		super(message);
	}

	public BDBException(String message, Throwable cause) {
		super(message, cause);
	}

	public BDBException(Throwable cause) {
		super(cause);
	}

	public BDBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
