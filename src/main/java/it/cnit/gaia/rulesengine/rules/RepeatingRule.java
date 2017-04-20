package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;

public class RepeatingRule extends CompositeRule{
	public long counter = 0;
	@LoadMe
	public long threshold = 0;
	//Injected during creation or istantiated by ClassName
	public GaiaRule rule;

	@Override
	public boolean condition() {
		updateCounter();
		return counter >= threshold;
	}

	@Override
	public void action() {
		resetCounter();
	}

	protected void updateCounter(){
		if( rule.condition() ){
			counter = ruleDatabaseService.incrementCounter(rid);
		}
		else {
			resetCounter();
		}
	}

	protected void resetCounter(){
		ruleDatabaseService.resetRuleCounter(rid);
	}

	@Override
	public boolean init(){
		boolean result = true;
		if(ruleSet.size()!=0)
			rule = ruleSet.iterator().next();

		if(rule==null){
			LOGGER.error("Rule not defined");
			result = false;
		}
		return result;
	}

}
