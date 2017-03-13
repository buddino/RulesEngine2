package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;

import java.util.Arrays;
import java.util.List;

public class SimpleThresholdRule extends GaiaRule {

	@LogMe
	@LoadMe
	@URI
	public String uri;
	@LogMe
	@LoadMe
	public Double threshold;
	@LogMe
	@LoadMe
	public String operator;
	@LogMe
	public Double value;

	List<String> validValues = Arrays.asList("==", ">", "<", "<=", ">=");

	@Override
	public boolean condition() {
		//TODO Get from measurements
		value = Math.random();
		switch (operator) {
			case "==":
				return Math.abs(value - threshold) < 0.001;
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
	public boolean init() throws Exception {
		if (validValues.stream().anyMatch(v -> v.equals(operator)))
			return true;
		throw new Exception("Threshold rule operator " + operator + " is not valid. Valid operators: " + validValues.toString());

	}


}
