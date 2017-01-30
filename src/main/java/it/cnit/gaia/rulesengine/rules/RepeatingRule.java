package it.cnit.gaia.rulesengine.rules;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
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
		OrientGraph orientdb = graphFactory.getTx();
		if( rule.condition() ){
			//TODO Manage field not found
			counter = orientdb.getVertex(rid).getProperty("counter");
			orientdb.getVertex(rid).setProperty("counter",++counter);
		}
		else {
			orientdb.getVertex(rid).setProperty("counter",0);
		}
		orientdb.commit();
	}

	protected void resetCounter(){
		OrientGraph orientdb = graphFactory.getTx();
		orientdb.getVertex(rid).setProperty("counter",0);
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
