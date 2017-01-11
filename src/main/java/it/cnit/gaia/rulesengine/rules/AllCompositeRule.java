package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;

public class AllCompositeRule extends CompositeRule{
	@Override
	public boolean condition() {
		return ruleSet.stream().allMatch(GaiaRule::condition);
	}
}
