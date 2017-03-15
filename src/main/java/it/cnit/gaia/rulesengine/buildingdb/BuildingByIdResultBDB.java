package it.cnit.gaia.rulesengine.buildingdb;

public class BuildingByIdResultBDB {
	String result;
	BuildingBDB item;
	String error;

	public String getError() {
		return error;
	}

	public BuildingByIdResultBDB setError(String error) {
		this.error = error;
		return this;
	}

	public String getResult() {
		return result;
	}

	public BuildingByIdResultBDB setResult(String result) {
		this.result = result;
		return this;
	}

	public BuildingBDB getItem() {
		return item;
	}

	public BuildingByIdResultBDB setItem(BuildingBDB item) {
		this.item = item;
		return this;
	}
}
