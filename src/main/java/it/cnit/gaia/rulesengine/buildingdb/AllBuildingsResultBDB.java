package it.cnit.gaia.rulesengine.buildingdb;

import java.util.List;

public class AllBuildingsResultBDB {
	String result;
	List<BuildingBDB> items;
	String error;

	public String getError() {
		return error;
	}

	public AllBuildingsResultBDB setError(String error) {
		this.error = error;
		return this;
	}
	public String getResult() {
		return result;
	}

	public AllBuildingsResultBDB setResult(String result) {
		this.result = result;
		return this;
	}

	public List<BuildingBDB> getItems() {
		return items;
	}

	public AllBuildingsResultBDB setItems(List<BuildingBDB> items) {
		this.items = items;
		return this;
	}
}
