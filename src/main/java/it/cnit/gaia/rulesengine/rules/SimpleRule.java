package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.FromConfiguration;
import it.cnit.gaia.rulesengine.model.GaiaRule;

public class SimpleRule extends GaiaRule{

	@FromConfiguration
	public Double threshold;
	@FromConfiguration
	public String uri;

	@Override
	public boolean condition() {
		return true;
	}

	@Override
	public void action() {
		System.out.println("Simple Rule triggered");
	}

	@Override
	public String toString() {
		return "SimpleRule:\t{" +
				"threshold:" + threshold +
				", uri:'" + uri + '\'' +
				'}';
	}
}
