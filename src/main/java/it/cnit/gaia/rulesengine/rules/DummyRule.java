package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.FromConfiguration;
import it.cnit.gaia.rulesengine.model.annotation.ToBeLogged;

public class DummyRule extends GaiaRule {

	@FromConfiguration
	@ToBeLogged
	public Double threshold;

	@FromConfiguration
	@ToBeLogged
	public String uri;

	@Override
	public boolean condition() {
		return Math.random() < threshold;
	}

	@Override
	public String toString() {
		return "DummyRule:\t{" +
				"pwf_threshold:" + threshold +
				", uri:'" + uri + '\'' +
				'}';
	}


}
