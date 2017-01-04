package model;

import it.cnit.gaia.rulesengine.model.GaiaRule;

public class RuleForTest extends GaiaRule {

	@Override
	public boolean condition() {
		return Math.random()<0.5;
	}

	@Override
	public void action() {
		System.out.println("Rule triggered.");
	}
}
