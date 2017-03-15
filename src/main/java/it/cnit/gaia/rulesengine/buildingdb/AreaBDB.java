package it.cnit.gaia.rulesengine.buildingdb;

import org.apache.commons.lang.builder.ToStringBuilder;

public class AreaBDB {
	String name;
	Long id;
	String description;
	String type;
	Object json;

	public String getName() {
		return name;
	}

	public AreaBDB setName(String name) {
		this.name = name;
		return this;
	}

	public Long getId() {
		return id;
	}

	public AreaBDB setId(Long id) {
		this.id = id;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public AreaBDB setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getType() {
		return type;
	}

	public AreaBDB setType(String type) {
		this.type = type;
		return this;
	}

	public Object getJson() {
		return json;
	}

	public AreaBDB setJson(Object json) {
		this.json = json;
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("name", name)
				.append("id", id)
				.append("description", description)
				.append("type", type)
				.append("json", json)
				.toString();
	}
}
