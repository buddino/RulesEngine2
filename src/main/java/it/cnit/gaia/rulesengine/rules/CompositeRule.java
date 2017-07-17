package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class CompositeRule extends GaiaRule{
	public Set<GaiaRule> ruleSet;

	@LogMe
	public List<Map<String,Object>> children;

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
