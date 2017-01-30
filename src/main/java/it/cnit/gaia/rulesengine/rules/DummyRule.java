package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;

public class DummyRule extends GaiaRule {

	@LoadMe
	@LogMe(event = false)
	public Double threshold;

	@LoadMe(required = false)
	@LogMe(event = false)
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
