package it.cnit.gaia.rulesengine.model;

import java.util.HashSet;
import java.util.Set;

public class GaiaRuleSet implements Fireable {
	Set<Fireable> ruleSet = new HashSet<>();
	//public void fire(){ ruleSet.parallelStream().forEach(f-> f.fire());}
	public void fire(){ ruleSet.forEach(f-> f.fire());}
	public boolean add(Fireable f){ return ruleSet.add(f);}
	public boolean remove(Fireable f){ return ruleSet.remove(f);}
}
