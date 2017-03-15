package it.cnit.gaia.rulesengine.buildingdb;

import java.util.List;

public class AllAreasResultBDB {
	String result;
	List<AreaBDB> items;
	String error;

	public String getError() {
		return error;
	}

	public AllAreasResultBDB setError(String error) {
		this.error = error;
		return this;
	}
	public String getResult() {
		return result;
	}

	public AllAreasResultBDB setResult(String result) {
		this.result = result;
		return this;
	}

	public List<AreaBDB> getItems() {
		return items;
	}

	public AllAreasResultBDB setItems(List<AreaBDB> items) {
		this.items = items;
		return this;
	}
}
