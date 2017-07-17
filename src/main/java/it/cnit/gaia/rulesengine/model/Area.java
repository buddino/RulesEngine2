package it.cnit.gaia.rulesengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import it.cnit.gaia.rulesengine.configuration.ContextProvider;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Area implements Fireable {

	public Long aid;
	public String rid;
	public String name;
	public String uri;
	public String type;
	/*
	public String json;
	public Double sqmt;
	public Long people;
	public String country;
	*/
	protected OrientGraphFactory graphFactory = ContextProvider.getBean(OrientGraphFactory.class);
	@JsonIgnore
	Set<Fireable> ruleSet = new HashSet<>();

	public void fire() {
		ruleSet.forEach(f -> f.fire());
	}

	@Override
	public boolean init() {
		return true;
	}

	public boolean add(Fireable f) {
		return ruleSet.add(f);
	}

	public boolean remove(Fireable f) {
		return ruleSet.remove(f);
	}

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

	//FIXME Should not stay here
	public String getPath() {
		OrientGraphNoTx noTx = graphFactory.getNoTx();
		ORID identity = noTx.getVertex(rid).getIdentity();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select unionall(name) as path from (traverse in() from ?)");
		List<ODocument> execute = query.execute(identity);
		List<String> path = execute.get(0).field("path");
		Collections.reverse(path);
		String uri = path.stream().collect(Collectors.joining("/"));
		return uri;
	}

	public Set<Fireable> getRuleSet() {
		return Collections.unmodifiableSet(ruleSet);
	}


}
