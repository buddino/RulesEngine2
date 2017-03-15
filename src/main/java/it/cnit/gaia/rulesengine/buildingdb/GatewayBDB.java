package it.cnit.gaia.rulesengine.buildingdb;

public class GatewayBDB {
	String name;
	Long id;
	Long building_id;
	String description;
	String producer;
	String uri_resource;
	String json;

	public String getName() {
		return name;
	}

	public GatewayBDB setName(String name) {
		this.name = name;
		return this;
	}

	public Long getId() {
		return id;
	}

	public GatewayBDB setId(Long id) {
		this.id = id;
		return this;
	}

	public Long getBuilding_id() {
		return building_id;
	}

	public GatewayBDB setBuilding_id(Long building_id) {
		this.building_id = building_id;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public GatewayBDB setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getProducer() {
		return producer;
	}

	public GatewayBDB setProducer(String producer) {
		this.producer = producer;
		return this;
	}

	public String getUri_resource() {
		return uri_resource;
	}

	public GatewayBDB setUri_resource(String uri_resource) {
		this.uri_resource = uri_resource;
		return this;
	}

	public String getJson() {
		return json;
	}

	public GatewayBDB setJson(String json) {
		this.json = json;
		return this;
	}
}
