package it.cnit.gaia.rulesengine.model;

/**
 * A Rule or a Set of rule
 */
public interface Fireable {
	void fire();

	/**
	 * Initilization, Validation and URIs injection into measurement repository
	 * @return true if the rule has been initialized succesfully
	 */
	boolean init();
	//Riguarda Initialization and validation inside the constructor and null check during class loading
}
