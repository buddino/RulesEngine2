package it.cnit.gaia.rulesengine.service;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class RuleDatabaseService {
	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private OrientGraphFactory ogf;

	public Long getParentArea(String ruleId) {
		OrientVertex ruleVertex = ogf.getNoTx().getVertex(ruleId);
		try {
			Vertex areaVertex = ruleVertex.getVertices(Direction.IN).iterator().next();
			Long aid = areaVertex.getProperty("aid");
			if (aid == null) {
				LOGGER.error(String.format("The rule %s is not connected to a valid area (no aid found)", ruleId));
				return null;
			}
			return aid;
		} catch (NoSuchElementException e) {
			LOGGER.error(String.format("The rule %s is not connected to an area", ruleId));
			return null;
		}
	}

	public String getRulePath(String ruleId){
		OrientGraphNoTx noTx = ogf.getNoTx();
		ORID identity = noTx.getVertex(ruleId).getIdentity();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select unionall(name) as path from (traverse in() from ?)");
		List<ODocument> execute = query.execute(identity);
		List<String> path = execute.get(0).field("path");
		Collections.reverse(path);
		String uri = path.stream().collect(Collectors.joining("/"));
		return uri;
	}

	public OrientGraphNoTx getGraphNoTx(){
		return ogf.getNoTx();
	}

	public OrientGraph getGraphTx(){
		return ogf.getTx();
	}

	public void resetRuleCounter(String ruleId){
		OrientGraph orientdb = ogf.getTx();
		orientdb.getVertex(ruleId).setProperty("counter",0);
		orientdb.commit();
	}

	public long incrementCounter(String ruleId){
		OrientGraph orientdb = ogf.getTx();
		long counter = orientdb.getVertex(ruleId).getProperty("counter");
		orientdb.getVertex(ruleId).setProperty("counter",++counter);
		orientdb.commit();
		return counter;
	}

}
