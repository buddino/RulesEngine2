package it.cnit.gaia.rulesengine.rules;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.CompositeRule;

@CompositeRule
public class RepeatingRule extends GaiaRule{
	public long counter;
	//@FromConfiuration
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
		System.out.println("CounterRule triggered");
		resetCounter();
	}

	protected void updateCounter(){
		OrientGraph orientdb = factory.getTx();
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
		OrientGraph orientdb = factory.getTx();
		orientdb.getVertex(rid).setProperty("counter",0);
	}
}
