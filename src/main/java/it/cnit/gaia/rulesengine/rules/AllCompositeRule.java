package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;

import java.util.ArrayList;

public class AllCompositeRule extends CompositeRule{
	@LogMe
	public int trueCounter = 0;

	@Override
	public boolean condition() {
		if (ruleSet.size() == 0)
			return false;
		children = new ArrayList<>();
		for (GaiaRule rule : ruleSet) {
			if (rule.condition()) {
				children.add(rule.getAllFields());
				trueCounter++;
			}
		}
		return trueCounter == ruleSet.size();
		//return ruleSet.stream().anyMatch(GaiaRule::condition);
	}
}
