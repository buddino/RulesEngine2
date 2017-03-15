package it.cnit.gaia.rulesengine.buildingdb;

public class GatewayByIdResultBDB {
	String result;
	GatewayBDB item;
	String error;

	public String getError() {
		return error;
	}

	public GatewayByIdResultBDB setError(String error) {
		this.error = error;
		return this;
	}

	public String getResult() {
		return result;
	}

	public GatewayByIdResultBDB setResult(String result) {
		this.result = result;
		return this;
	}

	public GatewayBDB getItem() {
		return item;
	}

	public GatewayByIdResultBDB setItem(GatewayBDB item) {
		this.item = item;
		return this;
	}
}
