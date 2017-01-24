package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.FromConfiguration;
import it.cnit.gaia.rulesengine.model.annotation.ToBeLogged;
import it.cnit.gaia.rulesengine.model.annotation.URI;

import java.util.Arrays;
import java.util.List;

public class SimpleThresholdRule extends GaiaRule{

	@ToBeLogged	@FromConfiguration @URI
	public String uri;
	@ToBeLogged	@FromConfiguration
	public Double threshold;
	@ToBeLogged	@FromConfiguration
	public String operator;
	@ToBeLogged
	public Double value;

	List<String> validValues = Arrays.asList("==",">","<","<=",">=");
	@Override
	public boolean condition() {
		//TODO Get from measurements
		value = Math.random();
		switch (operator){
			case "==":
				return Math.abs(value-threshold) < 0.001;
			case ">":
				return value > threshold;
			case "<":
				return value < threshold;
			case ">=":
				return value >= threshold;
			case "<=":
				return value <= threshold;
			default:
				return false;
		}
	}

	@Override
	public boolean init(){
		if(validValues.stream().anyMatch(v -> v.equals(operator)))
			return true;
		LOGGER.error("Threshold rule operator "+operator+" is not valid. Valid operators: "+validValues.toString());
		return false;
	}


}
