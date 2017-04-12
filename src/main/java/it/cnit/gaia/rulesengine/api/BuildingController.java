package it.cnit.gaia.rulesengine.api;

import com.google.gson.Gson;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OConcurrentResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.buildingdb.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.api.request.ErrorResponse;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import it.cnit.gaia.rulesengine.utils.BuildingUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class BuildingController {
	private Logger LOGGER = Logger.getRootLogger();
	private Gson g = new Gson();

	@Autowired
	private OrientGraphFactory graphFactory;
	@Autowired
	private RulesLoader rulesLoader;
	@Autowired
	private MeasurementRepository measurementRepository;
	@Autowired
	private BuildingUtils buildingUtils;


	@ExceptionHandler(BuildingDatabaseException.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(Exception e) {
		ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), e.getMessage());
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}

	@RequestMapping(value="/building/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<?> buildTreeFromBuildingDB(@PathVariable Long id) throws BuildingDatabaseException, IllegalAccessException {
		OrientVertex vertex = buildingUtils.buildTreeFromBuildingDB(id);
		return ResponseEntity.ok(vertex.getRecord().toJSON());
	}

	@RequestMapping(value="/building/{id}", method = RequestMethod.DELETE)
	public @ResponseBody ResponseEntity<?> deleteBuildingTree(@PathVariable Long id) throws BuildingDatabaseException, IllegalAccessException {
		buildingUtils.deleteBuildingTree(id);
		return ResponseEntity.ok(null);
	}

	@RequestMapping(value = "/building", method = RequestMethod.GET)
	public
	@ResponseBody
	Collection<School> getSchools() {
		return rulesLoader.loadSchools().values();
	}

	@RequestMapping(value = "/building/{sid}/area", method = RequestMethod.GET)
	public ResponseEntity<Object> getAreas(@PathVariable Long sid) {
		OrientGraphNoTx db = graphFactory.getNoTx();
		OSQLSynchQuery query = new OSQLSynchQuery("select * from (traverse * from (select from BuildingBDB where aid = ?)) where @class = \"Area\"");
		query.execute(sid);
		OConcurrentResultSet<ODocument> result = (OConcurrentResultSet<ODocument>) query.getResult();
		Map<String, String> areas = new HashMap<>();
		result.forEach(r -> areas.put(r.field("uri"), r.getIdentity().toString()));
		return ResponseEntity.status(HttpStatus.OK).body(areas);
	}

	@ResponseBody
	@RequestMapping(value = "/building/reload/{schoolId}", method = RequestMethod.GET)
	public ResponseEntity<String> reloadSchoolTree(@PathVariable Long schoolId) {
		if (!rulesLoader.reloadSchool(schoolId)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@ResponseBody
	@GetMapping("/building/reload")
	public ResponseEntity<String> reloadAllSchools() {
		rulesLoader.reloadAllSchools();
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/building/uris")
	public
	@ResponseBody ResponseEntity<Map<String, Long>> getUriMapping() {
		return ResponseEntity.ok(measurementRepository.getMeterMap());
	}

}
