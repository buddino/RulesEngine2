package it.cnit.gaia.rulesengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.*;

public class Area implements Fireable {

	public Long aid;
	public String rid;
	public String name;
	public String uri;
	public String type;
	@JsonProperty("json")
	public Map<String, Object> metadata = new HashMap<>();
	@JsonIgnore
	Set<Fireable> children = new HashSet<>();

	@Override
	public boolean init() {
		return true;
	}

	public void fire() {
		children.forEach(f -> f.fire());
	}

	public boolean add(Fireable f) {
		return children.add(f);
	}

	public boolean remove(Fireable f) {
		return children.remove(f);
	}

	public String getRid() {
		return rid;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", rid)
				.append("children", children)
				.toString();
	}

	public Set<Fireable> getChildren() {
		return Collections.unmodifiableSet(children);
	}

	public String getName() {
		return name;
	}

	public Long getAid() {
		return aid;
	}

	public Area setAid(Long aid) {
		this.aid = aid;
		return this;
	}

	public Area setRid(String rid) {
		this.rid = rid;
		return this;
	}

	public String getUri() {
		return uri;
	}

	public Area setUri(String uri) {
		this.uri = uri;
		return this;
	}

	public String getType() {
		return type;
	}

	public Area setType(String type) {
		this.type = type;
		return this;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public Area setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
		return this;
	}

	public Area setChildren(Set<Fireable> children) {
		this.children = children;
		return this;
	}

	public Area setName(String name) {
		this.name = name;
		return this;
	}
}
