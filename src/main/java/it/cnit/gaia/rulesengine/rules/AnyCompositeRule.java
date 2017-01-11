package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;

public class AnyCompositeRule extends CompositeRule{
	@Override
	public boolean condition() {
		return ruleSet.stream().anyMatch(GaiaRule::condition);
	}
}
