package it.cnit.gaia.rulesengine.api;

import com.google.gson.Gson;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.client.ApiException;
import it.cnit.gaia.rulesengine.api.request.CustomRuleException;
import it.cnit.gaia.rulesengine.api.request.ErrorResponse;
import it.cnit.gaia.rulesengine.api.request.NewRule;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import it.cnit.gaia.rulesengine.utils.DatabaseSchemaService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Api(produces = MediaType.APPLICATION_JSON_VALUE, value = "PROVA")
public class RulesController {
	private Logger LOGGER = Logger.getRootLogger();
	private Gson g = new Gson();

	@Autowired
	private OrientGraphFactory graphFactory;
	@Autowired
	private RulesLoader rulesLoader;
	@Autowired
	private MeasurementRepository measurementRepository;
	@Autowired
	private DatabaseSchemaService dbService;

	@ApiOperation(value = "Finds Pets by status",
			notes = "Multiple status values can be provided with comma seperated strings",
			response = ODocument.class,
			responseContainer = "List")
	@GetMapping(value = "/area/{id}/rules")
	@ResponseBody
	public ResponseEntity<List<ODocument>> getRuleOfArea(@PathVariable String id, @RequestParam(required = false, defaultValue = "false") Boolean traverse) throws CustomRuleException {
		OrientGraphNoTx g = graphFactory.getNoTx();
		Iterable<Vertex> result = g.getVertices("aid", id);
		if (!result.iterator()
				.hasNext()) {
			throw new CustomRuleException(String.format("Area %d not found", id));
		}
		OrientVertex areaV = (OrientVertex) result.iterator()
				.next();
		String statement;
		if (traverse)
			statement = "select * from (traverse out() from ?) where @this instanceof 'GaiaRule'";
		else
			statement = "select * from (traverse out() from ?) where @this instanceof 'GaiaRule' and $depth==1";
		OSQLSynchQuery query = new OSQLSynchQuery(statement);
		List<ODocument> rulesOfArea = (List<ODocument>) query.execute(areaV.getIdentity());
		return ResponseEntity.ok(rulesOfArea);

	}

	@DeleteMapping(value = "/rules/{rid}")
	@ResponseBody
	public ResponseEntity<String> deleteRule(@PathVariable String rid) {
		OrientGraph tx = graphFactory.getTx();
		OrientVertex v = tx.getVertex(rid);
		if (!(Boolean) v.getProperty("custom")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(null);
		}
		v.remove();
		tx.commit();
		tx.shutdown();
		return ResponseEntity.status(HttpStatus.NO_CONTENT)
				.body(null);
	}

	@RequestMapping(value = "/rules/{rid}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> editRule(@PathVariable String rid, @RequestBody NewRule newRule) throws Exception {
		OrientGraph tx = graphFactory.getTx();
		OrientVertex ruleVertex = null;
		Map<String, Object> fieldMap = newRule.rule;
		try {
			ruleVertex = tx.getVertex(rid);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(null);
		}
		if (!(Boolean) ruleVertex.getProperty("custom")) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(null);
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
		return ResponseEntity.status(HttpStatus.OK)
				.body(fieldMap);
	}

	@RequestMapping(value = "/rules", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> addRule(@RequestBody NewRule newRule) throws Exception {
		//Riguarda: molto codice
		OrientGraph tx = graphFactory.getTx();
		Iterable<Vertex> iterator;
		iterator = tx.getVertices("aid", newRule.school);
		OrientVertex school, areaVertex;
		if (iterator.iterator()
				.hasNext())
			//Used only to check if school exists
			school = (OrientVertex) iterator.iterator()
					.next();
		else {
			tx.rollback();
			throw new CustomRuleException(String.format("Building %d not found", newRule.school));
		}
		iterator = tx.getVertices("aid", newRule.area);
		if (iterator.iterator()
				.hasNext())
			areaVertex = (OrientVertex) iterator.iterator()
					.next();
		else {
			tx.rollback();
			throw new CustomRuleException(String.format("Parent area %d not found", newRule.area));
		}
		//Check URI
		String uri = (String) newRule.rule.get("uri");
		try {
			measurementRepository.checkUri(uri);
		} catch (ApiException e) {
			tx.rollback();
			throw new CustomRuleException(String.format("URI '%s' not found", uri));
		}

		//Load rule
		Map<String, Object> fieldMap = newRule.rule;
		OrientVertex ruleVertex = tx.addVertex("class:" + fieldMap.get("@class")
				.toString());
		ruleVertex.setProperties(fieldMap);
		ruleVertex.setProperty("custom", true);
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
		fieldMap.put("@rid", ruleVertex.getIdentity()
				.toString());
		tx.shutdown();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(fieldMap);
	}

	@ExceptionHandler(CustomRuleException.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(Exception e) {
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

}
