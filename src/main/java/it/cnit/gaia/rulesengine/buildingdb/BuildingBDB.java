package it.cnit.gaia.rulesengine.buildingdb;

import org.apache.commons.lang.builder.ToStringBuilder;

public class BuildingBDB {
	Long id;
	String name;
	Double sqmt;
	Long people;
	String country;

	public Long getId() {
		return id;
	}

	public BuildingBDB setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public BuildingBDB setName(String name) {
		this.name = name;
		return this;
	}

	public Double getSqmt() {
		return sqmt;
	}

	public BuildingBDB setSqmt(Double sqmt) {
		this.sqmt = sqmt;
		return this;
	}

	public Long getPeople() {
		return people;
	}

	public BuildingBDB setPeople(Long people) {
		this.people = people;
		return this;
	}

	public String getCountry() {
		return country;
	}

	public BuildingBDB setCountry(String country) {
		this.country = country;
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("name", name)
				.append("sqmt", sqmt)
				.append("people", people)
				.append("country", country)
				.toString();
	}
}
