package it.cnit.gaia.rulesengine.buildingdb;

public class AreaByIdResultBDB {
	String result;
	AreaBDB item;
	String error;

	public String getError() {
		return error;
	}

	public AreaByIdResultBDB setError(String error) {
		this.error = error;
		return this;
	}

	public String getResult() {
		return result;
	}

	public AreaByIdResultBDB setResult(String result) {
		this.result = result;
		return this;
	}

	public AreaBDB getItem() {
		return item;
	}

	public AreaByIdResultBDB setItem(AreaBDB item) {
		this.item = item;
		return this;
	}
}
