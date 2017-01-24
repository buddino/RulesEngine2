package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;

import java.util.Collections;
import java.util.Set;

public abstract class CompositeRule extends GaiaRule{
	public Set<GaiaRule> ruleSet;
	public boolean addToSet(GaiaRule rule){
		return ruleSet.add(rule);
	}
	public boolean removeFromSet(GaiaRule rule){
		return ruleSet.remove(rule);
	}

	public Set<GaiaRule> getRuleSet() {
		return Collections.unmodifiableSet(ruleSet);
	}
}
