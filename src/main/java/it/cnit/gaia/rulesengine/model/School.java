package it.cnit.gaia.rulesengine.model;

public class School {
	String name;
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

	public void fire(){
		root.fire();
	}

}
