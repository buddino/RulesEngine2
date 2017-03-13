package it.cnit.gaia.rulesengine.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import io.swagger.client.ApiException;
import it.cnit.gaia.rulesengine.api.request.CustomRuleException;
import it.cnit.gaia.rulesengine.api.request.ErrorResponse;
import it.cnit.gaia.rulesengine.api.request.NewRule;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.Fireable;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.GaiaRuleSet;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.model.errors.ResourceNotFoundException;
import it.cnit.gaia.rulesengine.rules.CompositeRule;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@RestController
public class RESTController {
	private Logger LOGGER = Logger.getRootLogger();
	private Gson g = new Gson();

	@Autowired
	private OrientGraphFactory graphFactory;

	@Autowired
	private RulesLoader rulesLoader;

	@Autowired
	private MeasurementRepository measurementRepository;

	@RequestMapping(value = "/rules/{schoolId}", method = RequestMethod.GET)
	public String getrulestree(@PathVariable Long schoolId) {
		School school = rulesLoader.loadSchools().get(schoolId);
		if (school == null)
			throw new ResourceNotFoundException();
		Fireable root = school.getRoot();
		JsonElement json = traverse(root);
		return json.toString();
	}


	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/rules/{rid}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<String> deleteRule(@PathVariable String rid) {
		OrientGraph tx = graphFactory.getTx();
		OrientVertex v = tx.getVertex(rid);
		if(!(Boolean)v.getProperty("custom")){
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		v.remove();
		tx.commit();
		tx.shutdown();
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
	}

	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/rules/{rid}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Object> editRule(@PathVariable String rid, @RequestBody NewRule newRule) throws Exception {
		OrientGraph tx = graphFactory.getTx();
		OrientVertex ruleVertex = null;
		Map<String, Object> fieldMap = newRule.rule;
		try {
			ruleVertex = tx.getVertex(rid);
		}
		catch (Exception e){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		if(!(Boolean)ruleVertex.getProperty("custom")){
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}

		String uri = (String) newRule.rule.get("uri");
		try {
			measurementRepository.checkUri(uri);
		} catch (ApiException e) {
			tx.rollback();
			throw new CustomRuleException(String.format("URI '%s' not found", uri));
		}

		ruleVertex.setProperties(fieldMap);

		GaiaRule javaRule = rulesLoader.getRuleForTest(ruleVertex);
		try {
			javaRule.init();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		}

		tx.commit();
		tx.shutdown();
		return ResponseEntity.status(HttpStatus.CREATED).body(fieldMap);
	}

	@RequestMapping(value = "/rules", method = RequestMethod.POST, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<Object> addRule(@RequestBody NewRule newRule) throws Exception {
		OrientGraph tx = graphFactory.getTx();
		Iterable<Vertex> iterator;
		iterator = tx.getVertices("aid", newRule.school);
		OrientVertex school, areaVertex;
		if (iterator.iterator().hasNext())
			school = (OrientVertex) iterator.iterator().next();
		else {
			tx.rollback();
			throw new CustomRuleException(String.format("School %d not found", newRule.school));
		}
		iterator = tx.getVertices("aid", newRule.parent);
		if (iterator.iterator().hasNext())
			areaVertex = (OrientVertex) iterator.iterator().next();
		else {
			tx.rollback();
			throw new CustomRuleException(String.format("Parent area %d not found", newRule.parent));
		}

		String uri = (String) newRule.rule.get("uri");
		try {
			measurementRepository.checkUri(uri);
		} catch (ApiException e) {
			tx.rollback();
			throw new CustomRuleException(String.format("URI '%s' not found", uri));
		}

		//Load rule
		Map<String, Object> fieldMap = newRule.rule;
		OrientVertex ruleVertex = tx.addVertex("class:" + fieldMap.get("@class").toString());
		ruleVertex.setProperties(fieldMap);
		ruleVertex.setProperty("custom",true);
		ruleVertex.save();

		GaiaRule javaRule = rulesLoader.getRuleForTest(ruleVertex);
		try {
			javaRule.init();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		}

		tx.addEdge(null, areaVertex, ruleVertex, "E");
		tx.commit();
		fieldMap.put("@rid",ruleVertex.getIdentity().toString());
		tx.shutdown();
		return ResponseEntity.status(HttpStatus.CREATED).body(fieldMap);
	}

	@ExceptionHandler(CustomRuleException.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(Exception e) {
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/schools", method = RequestMethod.GET)
	public
	@ResponseBody
	Collection<School> getSchools() {
		return rulesLoader.loadSchools().values();
	}

	@ResponseBody
	@RequestMapping(value = "/reload/{schoolId}", method = RequestMethod.GET)
	public ResponseEntity<String> reload(@PathVariable Long schoolId) {
		if (!rulesLoader.reloadSchool(schoolId)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping("/reload")
	public ResponseEntity<String> reload() {
		rulesLoader.reloadAllSchools();
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping("/uris")
	public
	@ResponseBody
	Map<String, Long> getUriMapping() {
		return measurementRepository.getMeterMap();
	}

	public JsonElement traverse(Fireable root) {
		//Riguarda composite e ruleset forse ne basta uno solo
		if (root instanceof GaiaRuleSet) {
			JsonObject obj = new JsonObject();
			obj.addProperty("rule", root.getClass().getSimpleName());
			obj.addProperty("rid", ((GaiaRuleSet) root).getRid());
			JsonArray arr = new JsonArray();
			Set<Fireable> ruleSet = ((GaiaRuleSet) root).getRuleSet();
			for (Fireable f : ruleSet) {
				arr.add(traverse(f));
			}
			obj.add("rules", arr);
			return obj;
		} else if (root instanceof CompositeRule) {
			JsonObject obj = new JsonObject();
			obj.addProperty("rule", root.getClass().getSimpleName());
			obj.addProperty("rid", ((CompositeRule) root).getRid());
			JsonArray arr = new JsonArray();
			Set<GaiaRule> ruleSet = ((CompositeRule) root).getRuleSet();
			for (Fireable f : ruleSet) {
				arr.add(traverse(f));
			}
			obj.add("rules", arr);
			return obj;
		} else {
			JsonObject obj = new JsonObject();
			obj.addProperty("rule", ((GaiaRule) root).getClass().getSimpleName());
			obj.addProperty("rid", ((GaiaRule) root).getRid());
			return obj;
		}
	}


}
