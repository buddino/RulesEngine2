package it.cnit.gaia.rulesengine.service;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OConcurrentResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import io.swagger.sparks.ApiException;
import it.cnit.gaia.rulesengine.api.dto.DefaultsDTO;
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

	public void reloadAllSchools(Boolean reloadnow) {
		rulesLoader.reloadAllSchools();
		if (reloadnow)
			rulesLoader.loadSchools();
	}

	public List<String> getEmail(String rid) {
		try {
			RuleDTO ruleFromDb = getRuleFromDb(rid);
			Map<String, Object> fields = ruleFromDb.getFields();
			String email = (String) fields.getOrDefault("email", null);
			if (email == null)
				return null;
			String[] split = email.split(",|;");
			return Arrays.asList(split);
		} catch (GaiaRuleException e) {
			LOGGER.error("Sending mail: " + e.getMessage());
		}
		return null;
	}

	@Deprecated
	public Long getParentArea(String aid) {
		//TODO
		return null;
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
		try {
			long counter = orientdb.getVertex(ruleId).getProperty("counter");
			orientdb.getVertex(ruleId).setProperty("counter", ++counter);
			return counter;
		} finally {
			orientdb.shutdown();
		}
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
			String uri = (String) ruleDTO.getFields().get("uri");
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
		//Remove null fields
		for(Map.Entry<String,Object> e : fieldMap.entrySet()){
			if(e.getValue()==null)
				fieldMap.remove(e.getKey());
		}
		ruleVertex.setProperties(fieldMap);
		ruleVertex.setProperty("custom", true);
		ruleVertex.setProperty("enabled", true);
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

	public RuleDTO editCustomRule(String rid, RuleDTO ruleDTO) throws GaiaRuleException {
		OrientGraph tx = ogf.getTx();
		OrientVertex ruleVertex;
		Map<String, Object> fieldMap = ruleDTO.getFields();
		ruleVertex = tx.getVertex(rid);
		Set<String> propertyKeys = ruleVertex.getPropertyKeys();
		propertyKeys.forEach(key -> ruleVertex.removeProperty(key));
		ruleVertex.setProperties(fieldMap);
		GaiaRule javaRule = rulesLoader.getRuleForTest(ruleVertex);
		try {
			javaRule.init();
		} catch (Exception e) {
			tx.rollback();
			throw new GaiaRuleException("Initialization error", HttpStatus.BAD_REQUEST);
		}
		ruleDTO.setRid(ruleVertex.getIdentity().toString());
		tx.commit();
		tx.shutdown();
		return ruleDTO;
	}

	public RuleDTO patchCustomRule(String rid, RuleDTO ruleDTO) throws GaiaRuleException {
		//TODO API for changing Area
		OrientGraph tx = ogf.getTx();
		OrientVertex ruleVertex;
		Map<String, Object> fieldMap = ruleDTO.getFields();
		ruleVertex = tx.getVertex(rid);
		ruleVertex.setProperties(fieldMap);
		GaiaRule javaRule = rulesLoader.getRuleForTest(ruleVertex);
		try {
			javaRule.init();
		} catch (Exception e) {
			tx.rollback();
			throw new GaiaRuleException("Initialization error", HttpStatus.BAD_REQUEST);
		}
		ruleDTO.setRid(ruleVertex.getIdentity().toString());
		tx.commit();
		tx.shutdown();
		RuleDTO resp = getRuleFromDb(ruleVertex.getIdentity().toString());
		return resp;
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
		fields.put("name", d.field("name"));
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

	public void setLatestFireTime(String rid, Date date) {
		OrientGraphNoTx tx = ogf.getNoTx();
		try {
			OrientVertex vertex = tx.getVertex(rid);
			vertex.setProperty("latestFireTime", date);
		} catch (OConcurrentModificationException e) {
			tx.getVertex(rid).setProperty("latestFireTime", date);
		}
		tx.shutdown();
	}

	public void setLatestTriggerTime(String rid, Date date) {
		OrientGraphNoTx tx = ogf.getNoTx();
		try {
			OrientVertex vertex = tx.getVertex(rid);
			vertex.setProperty("latestTriggerTime", date);
		} catch (OConcurrentModificationException e) {
			tx.getVertex(rid).setProperty("latestTriggerTime", date);
		}
		tx.shutdown();
	}

	public Date getLatestFireTime(String rid) {
		OrientGraphNoTx noTx = ogf.getNoTx();
		OrientVertex vertex = noTx.getVertex(rid);
		try {
			Date latestFireTime = vertex.getProperty("latestFireTime");
			return latestFireTime;
		} catch (ClassCastException e) {
			Date latestFireTime = new Date((Long) vertex.getProperty("latestFireTime"));
			return latestFireTime;
		} finally {
			noTx.shutdown();
		}
	}

	public Date getLatestTriggerTime(String rid) {
		OrientGraphNoTx noTx = ogf.getNoTx();
		OrientVertex vertex = noTx.getVertex(rid);
		try {
			Date latestTriggerTime = vertex.getProperty("latestTriggerTime");
			return latestTriggerTime;
		} catch (ClassCastException e) {
			Date latestTriggerTime = new Date((Long) vertex.getProperty("latestTriggerTime"));
			return latestTriggerTime;
		} finally {
			noTx.shutdown();
		}
	}

	public School getSchool(Long aid) {
		return rulesLoader.loadSchools().get(aid);
	}

	public RuleDTO addCustomRuleToComposite(String rid, RuleDTO ruleDTO) throws GaiaRuleException {
		OrientGraphNoTx tx = ogf.getNoTx();
		OrientVertex composite = tx.getVertex(rid);
		if (composite == null)
			throw new GaiaRuleException("Parent rule not found", HttpStatus.NOT_FOUND);
		OSQLSynchQuery query = new OSQLSynchQuery("select * from CompositeRule where @rid=?");
		List<ODocument> result = (List<ODocument>) query.execute(rid);
		if (result.size() == 0)
			throw new GaiaRuleException("The parent rule is not a CompositeRule", HttpStatus.NOT_FOUND);

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

	public DefaultsDTO getDefault(String classname) {
		ODatabaseDocumentTx database = ogf.getDatabase();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("SELECT * FROM GaiaDefaults where classname = ?");
		OConcurrentResultSet resultSet = query.execute(classname);
		if (resultSet.size() == 0)
			return null;
		Map<String, Object> res = ((ODocument) resultSet.get(0)).toMap();
		Map<String, Map<String, Object>> fields = (Map<String, Map<String, Object>>) res.get("fields");
		Map<String,String> suggestion = (Map<String, String>) res.get("suggestion");
		DefaultsDTO defaults = new DefaultsDTO();
		defaults.setFields(fields);
		defaults.setSuggestion(suggestion);
		return defaults;
	}


	public void deleteDefault(String classname) throws GaiaRuleException {
		ODatabaseDocumentTx database = ogf.getDatabase();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("SELECT * FROM GaiaDefaults where classname = ?");
		OConcurrentResultSet resultSet = query.execute(classname);
		if(resultSet.size()==1){
			ODocument o = (ODocument) resultSet.get(0);
			o.delete();
			return;
		}
		throw new GaiaRuleException("Not found",HttpStatus.NOT_FOUND);
	}

	public DefaultsDTO editDefault(String classname, DefaultsDTO defaults) throws GaiaRuleException {
		ODatabaseDocumentTx database = ogf.getDatabase();
		OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("SELECT * FROM GaiaDefaults where classname = ?");
		OConcurrentResultSet resultSet = query.execute(classname);
		if(resultSet.size()==1){
			ODocument o = (ODocument) resultSet.get(0);
			o.field("fields", defaults.getFields());
			o.field("suggestion", defaults.getSuggestion());
			o.save();
			return defaults;
		}
		else
			return addDefault(classname,defaults);
	}

	//TODO Patch

	public DefaultsDTO addDefault(String classname, DefaultsDTO defaults) throws GaiaRuleException {
		if(getDefault(classname)!=null)
			throw new GaiaRuleException("Conflict, default alredy present, use PUT to edit!", HttpStatus.CONFLICT);
		ODatabaseDocumentTx database = ogf.getDatabase();
		ODocument d = new ODocument("GaiaDefaults");
		d.field("classname", classname);
		//TODO Add control on the classname
		d.field("fields", defaults.getFields());
		d.field("suggestion", defaults.getSuggestion());
		d.save();
		return defaults;
	}


}
