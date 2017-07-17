package it.cnit.gaia.rulesengine.service;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import io.swagger.client.ApiException;
import it.cnit.gaia.rulesengine.api.dto.RuleDTO;
import it.cnit.gaia.rulesengine.api.exception.GaiaRuleException;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RuleDatabaseService {
	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private final List<String> skipFields = Arrays.asList("in_", "out_");

	@Autowired
	private OrientGraphFactory ogf;

	@Autowired
	private MeasurementRepository measurementRepository;

	@Autowired
	private RulesLoader rulesLoader;

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

	public String getParentAreaType(Long aid) {
		return rulesLoader.getAreaMap().get(aid).type;
	}

	//Riguarda
	public String getParentAreaType(String arid) {
		Long aid = getParentArea(arid);
		return rulesLoader.getAreaMap().get(aid).type;
	}

	public String getRulePath(String ruleId) {
		OrientGraphNoTx noTx = ogf.getNoTx();
		ORID identity = noTx.getVertex(ruleId).getIdentity();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("select unionall(name) as path from (traverse in() from ?)");
		List<ODocument> execute = query.execute(identity);
		List<String> path = execute.get(0).field("path");
		Collections.reverse(path);
		String uri = path.stream().collect(Collectors.joining("/"));
		return uri;
	}

	public OrientGraphNoTx getGraphNoTx() {
		return ogf.getNoTx();
	}

	public OrientGraph getGraphTx() {
		return ogf.getTx();
	}

	public void resetRuleCounter(String ruleId) {
		OrientGraph orientdb = ogf.getTx();
		orientdb.getVertex(ruleId).setProperty("counter", 0);
		orientdb.commit();
	}

	public long incrementCounter(String ruleId) {
		OrientGraph orientdb = ogf.getTx();
		long counter = orientdb.getVertex(ruleId).getProperty("counter");
		orientdb.getVertex(ruleId).setProperty("counter", ++counter);
		orientdb.commit();
		return counter;
	}

	public List<RuleDTO> getRulesForArea(Long aid, Boolean traverse) throws GaiaRuleException {
		OrientGraphNoTx g = ogf.getNoTx();
		Iterable<Vertex> result = g.getVertices("aid", aid);
		if (!result.iterator()
				   .hasNext()) {
			throw new GaiaRuleException(String.format("Area %d not found", aid), 404);
		}
		OrientVertex areaV = (OrientVertex) result.iterator().next();
		String statement;
		if (traverse)
			statement = "select * from (traverse out() from ?) where @this instanceof 'GaiaRule'";
		else
			statement = "select * from (traverse out() from ?) where @this instanceof 'GaiaRule' and $depth==1";
		OSQLSynchQuery query = new OSQLSynchQuery(statement);
		List<ODocument> docsOfArea = (List<ODocument>) query.execute(areaV.getIdentity());
		List<RuleDTO> rulesForArea = new ArrayList<>();
		for (ODocument d : docsOfArea) {
			RuleDTO ruleDTO = convertODocument2RuleDTO(d);
			rulesForArea.add(ruleDTO);
		}
		return rulesForArea;
	}

	public void deleteRule(String rid) throws GaiaRuleException {
		//FIXME Remove all the linked rules for composite rules
		OrientGraph tx = ogf.getTx();
		OrientVertex v = tx.getVertex(rid);
		if (!(Boolean) v.getProperty("custom")) {
			throw new GaiaRuleException("Trying to delete a non-custom rule", HttpStatus.UNAUTHORIZED);
		}
		v.remove();
		tx.commit();
		tx.shutdown();
	}

	public RuleDTO addCustomRuleToArea(Long aid, RuleDTO ruleDTO) throws GaiaRuleException, RuleInitializationException {
		OrientGraph tx = ogf.getTx();
		Iterable<Vertex> iterator;
		OrientVertex areaVertex;
		iterator = tx.getVertices("aid", aid);
		if (iterator.iterator().hasNext())
			areaVertex = (OrientVertex) iterator.iterator().next();
		else {
			tx.rollback();
			throw new GaiaRuleException(String.format("Parent area %d not found", aid));
		}


		//Check URI
		//FIXME Maybe not required beacuse the next control with init
		if (ruleDTO.getClazz().equals("SimpleThresholdRule")) {
			String uri = (String) ruleDTO.getFields().get("power_uri");
			try {
				measurementRepository.checkUri(uri);
			} catch (ApiException e) {
				tx.rollback();
				throw new GaiaRuleException(String.format("URI '%s' not found", uri), HttpStatus.BAD_REQUEST.value());
			}
		}


		//Load rule
		Map<String, Object> fieldMap = ruleDTO.getFields();
		OrientVertex ruleVertex = tx.addVertex("class:" + ruleDTO.getClazz());
		ruleVertex.setProperties(fieldMap);
		ruleVertex.setProperty("custom", true);
		ruleVertex.save();

		GaiaRule javaRule = rulesLoader.getRuleForTest(ruleVertex);
		try {
			javaRule.init();
		} catch (RuleInitializationException e) {
			tx.rollback();
			throw e;
		}

		tx.addEdge(null, areaVertex, ruleVertex, "E");
		tx.commit();
		fieldMap.put("@rid", ruleVertex.getIdentity()
									   .toString());
		ruleDTO.setRid(ruleVertex.getIdentity().toString());
		tx.shutdown();
		return ruleDTO;
	}

	public void editCustomRule(String rid, RuleDTO ruleDTO) throws GaiaRuleException {
		//TODO API for changing Area
		OrientGraph tx = ogf.getTx();
		OrientVertex ruleVertex;
		Map<String, Object> fieldMap = ruleDTO.getFields();
		try {
			ruleVertex = tx.getVertex(rid);
		} catch (Exception e) {
			throw new GaiaRuleException("Not found", 404);
		}
		if (!(Boolean) ruleVertex.getProperty("custom")) {
			throw new GaiaRuleException("Not authorized", 403);
		}
		String uri = (String) ruleDTO.getFields().get("power_uri");
		try {
			measurementRepository.checkUri(uri);
		} catch (ApiException e) {
			tx.rollback();
			throw new GaiaRuleException(String.format("URI '%s' not found", uri));
		}
		ruleVertex.setProperties(fieldMap);
		GaiaRule javaRule = rulesLoader.getRuleForTest(ruleVertex);
		try {
			javaRule.init();
		} catch (Exception e) {
			tx.rollback();
			throw new GaiaRuleException("Initialization error", HttpStatus.BAD_REQUEST);
		}
		tx.commit();
		tx.shutdown();
	}

	public RuleDTO getRuleFromDb(String rid) throws GaiaRuleException {
		OrientVertex vertex = ogf.getNoTx().getVertex(rid);
		if (vertex == null)
			throw new GaiaRuleException("Rule not found", HttpStatus.NOT_FOUND);
		return convertODocument2RuleDTO(vertex.getRecord());
	}

	public RuleDTO convertODocument2RuleDTO(ODocument d) {
		RuleDTO ruleDTO = new RuleDTO();
		ruleDTO.setClazz(d.getClassName());
		ruleDTO.setRid(d.getIdentity().toString());
		Map<String, Object> fields = new HashMap<>();
		for (String key : d.fieldNames()) {
			if (!skipFields.contains(key)) {
				Object o = d.field(key);
				if (o instanceof OIdentifiable)
					o = ((ODocument) o).getIdentity().toString();
				fields.put(key, o);
			}
		}
		fields.put("path", getRulePath(ruleDTO.getRid()));
		ruleDTO.setFields(fields);
		return ruleDTO;
	}

	public GaiaRule getRuleFromRuntime(String rid) {
		return rulesLoader.getGaiaRuleInstance(rid);
	}

	public void setLatestFireTime(String rid, Date date){
		OrientVertex vertex = ogf.getNoTx().getVertex(rid);
		vertex.setProperty("latestFireTime",date);
		vertex.save();
	}

	public Date getLatestFireTime(String rid){
		OrientVertex vertex = ogf.getNoTx().getVertex(rid);
		Date latestFireTime = vertex.getProperty("latestFireTime");
		return latestFireTime;
	}

	public School getSchool(Long aid) {
		return rulesLoader.loadSchools().get(aid);
	}

	public RuleDTO addCustomRuleToComposite(String rid, RuleDTO ruleDTO) throws GaiaRuleException {
		OrientGraph tx = ogf.getTx();
		OrientVertex composite = tx.getVertex(rid);
		if (composite == null)
			throw new GaiaRuleException("Parent rule not found", HttpStatus.NOT_FOUND);
		OSQLSynchQuery query = new OSQLSynchQuery("select * from CompositeRule where @rid=?");
		List<ODocument> result = (List<ODocument>) query.execute(rid);
		if (result.size() == 0)
			throw new GaiaRuleException("The parent rule is not a CompsoiteRule", HttpStatus.NOT_FOUND);

		Map<String, Object> fieldMap = ruleDTO.getFields();
		OrientVertex ruleVertex = tx.addVertex("class:" + ruleDTO.getClazz());
		ruleVertex.setProperties(fieldMap);
		ruleVertex.setProperty("custom", true);
		ruleVertex.save();

		GaiaRule javaRule = rulesLoader.getRuleForTest(ruleVertex);
		try {
			javaRule.init();
		} catch (RuleInitializationException e) {
			tx.rollback();
			throw new GaiaRuleException(e);
		}

		tx.addEdge(null, composite, ruleVertex, "E");
		tx.commit();
		fieldMap.put("@rid", ruleVertex.getIdentity()
									   .toString());
		ruleDTO.setRid(ruleVertex.getIdentity().toString());
		tx.shutdown();
		return ruleDTO;
	}
}
