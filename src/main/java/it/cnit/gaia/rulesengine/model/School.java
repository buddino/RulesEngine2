package it.cnit.gaia.rulesengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class School {
	String name;
	String id;
	String rid;

	@JsonIgnore
	Fireable root;

	public String getName() {
		return name;
	}

	public School setName(String name) {
		this.name = name;
		return this;
	}

	public Fireable getRoot() {
		return root;
	}

	public School setRoot(Fireable root) {
		this.root = root;
		return this;
	}

	public String getId() {
		return id;
	}

	public School setId(String id) {
		this.id = id;
		return this;
	}

	public void fire(){
		root.fire();
	}

	public String getRid() {
		return rid;
	}

	public School setRid(String rid) {
		this.rid = rid;
		return this;
	}
}
