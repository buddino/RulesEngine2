package it.cnit.gaia.rulesengine.model;

public class School extends Area{
	public String name;
	public Double lat,lon;

	public String getName() {
		return name;
	}

	public School setName(String name) {
		this.name = name;
		return this;
	}

	public Double getLat() {
		return lat;
	}

	public School setLat(Double lat) {
		this.lat = lat;
		return this;
	}

	public Double getLon() {
		return lon;
	}

	public School setLon(Double lon) {
		this.lon = lon;
		return this;
	}
}
