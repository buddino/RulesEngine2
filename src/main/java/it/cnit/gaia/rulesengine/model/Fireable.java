package it.cnit.gaia.rulesengine.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;

/**
 * A Rule or a Set of rule
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="@class", visible = true)
@JsonSubTypes.Type(value = AreaDepth.class, name = "Area")
public interface Fireable {
	void fire();

	/**
	 * Initilization, Validation and URIs injection into measurement repository
	 * @return true if the rule has been initialized succesfully
	 */
	boolean init() throws RuleInitializationException;
	//Riguarda Initialization and validation inside the constructor and null check during class loading
}
