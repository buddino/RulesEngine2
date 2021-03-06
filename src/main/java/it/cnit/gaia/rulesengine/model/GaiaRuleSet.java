package it.cnit.gaia.rulesengine.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GaiaRuleSet implements Fireable {
	public String rid;
	Set<Fireable> ruleSet = new HashSet<>();
	//public void fire(){ ruleSet.parallelStream().forEach(f-> f.fire());}
	public void fire(){ ruleSet.forEach(f-> f.fire());}

	@Override
	public boolean init() {
		return true;
	}

	public boolean add(Fireable f){ return ruleSet.add(f);}
	public boolean remove(Fireable f){ return ruleSet.remove(f);}

	public String getRid() {
		return rid;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", rid)
				.append("ruleSet", ruleSet)
				.toString();
	}

	public Set<Fireable> getRuleSet() {
		return Collections.unmodifiableSet(ruleSet);
	}
}
