package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.FromConfiguration;

public class SimpleRule extends GaiaRule{

	@FromConfiguration
	public Double threshold;

	@FromConfiguration
	public String uri;

	@Override
	public boolean condition() {
		return Math.random() < threshold;
	}

	@Override
	public void action() {
		System.out.println("Rule "+rid+" triggered!");
	}

	@Override
	public String toString() {
		return "SimpleRule:\t{" +
				"pwf_threshold:" + threshold +
				", uri:'" + uri + '\'' +
				'}';
	}
}
