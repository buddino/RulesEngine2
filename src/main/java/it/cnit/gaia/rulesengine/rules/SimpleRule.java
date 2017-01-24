package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.FromConfiguration;
import it.cnit.gaia.rulesengine.model.annotation.URI;

public class SimpleRule extends GaiaRule{

	@FromConfiguration
	public Double threshold;

	@FromConfiguration @URI
	public String uri;

	@Override
	public boolean condition() {
		return Math.random() < threshold;
	}

	@Override
	public String toString() {
		return "SimpleRule:\t{" +
				"pwf_threshold:" + threshold +
				", uri:'" + uri + '\'' +
				'}';
	}

	@Override
	public boolean init(){
		return threshold!=null && uri!=null && !uri.equals("");
	}
}
