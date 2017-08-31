package it.cnit.gaia.rulesengine.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AreaDepth implements Fireable{

	public Long aid;
	public String rid;
	public String name;
	public String uri;
	public String type;
	@JsonProperty("json")
	public Map<String, Object> metadata = new HashMap<>();
	public Set<Fireable> children = new HashSet<>();


	public AreaDepth(Area a){
		this.aid = a.aid;
		this.rid = a.rid;
		this.name = a.name;
		this.uri = a.uri;
		this.type = a.type;
		this.metadata = a.metadata;
		for(Fireable f : a.children){
			if( f instanceof Area) {
				Fireable x = new AreaDepth((Area) f);
				this.children.add(x);
			}
			else {
				this.children.add(f);
			}
		}
	}


	@Override
	public void fire() {
		System.err.println("AreaDepth CANNOT BE FIRED");
	}

	@Override
	public boolean init() throws RuleInitializationException {
		System.err.println("AreaDepth CANNOT BE INITIALIZED");
		return false;
	}
}
