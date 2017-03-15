package it.cnit.gaia.rulesengine.buildingdb;

import java.util.List;

public class AllGatewaysResultBDB {
	String result;
	List<GatewayBDB> items;
	String error;

	public String getError() {
		return error;
	}

	public AllGatewaysResultBDB setError(String error) {
		this.error = error;
		return this;
	}

	public String getResult() {
		return result;
	}

	public AllGatewaysResultBDB setResult(String result) {
		this.result = result;
		return this;
	}

	public List<GatewayBDB> getItems() {
		return items;
	}

	public AllGatewaysResultBDB setItems(List<GatewayBDB> items) {
		this.items = items;
		return this;
	}
}
