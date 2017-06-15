package it.cnit.gaia.rulesengine.api;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import io.swagger.annotations.*;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.Area;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.utils.BuildingUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "Building",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class BuildingController {
	private Logger LOGGER = Logger.getRootLogger();
	private Gson g = new Gson();

	@Autowired
	private OrientGraphFactory graphFactory;
	@Autowired
	private RulesLoader rulesLoader;
	@Autowired
	private BuildingUtils buildingUtils;

	/*@ExceptionHandler(BuildingDatabaseException.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(Exception e) {
		ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), e.getMessage());
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}*/

	@PutMapping(value = "/building/{id}")
	@ApiOperation(
			value = "Import building into rule database",
			notes = "Import the building (indentified by id) from the building database replicating its structure")
	public
	@ResponseBody
	ResponseEntity<Void> buildTreeFromBuildingDB(@ApiParam("ID of the building") @PathVariable Long id) throws IllegalAccessException, BuildingDatabaseException {
		OrientVertex vertex = buildingUtils.buildTreeFromBuildingDB(id);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping(value = "/building/{id}")
	@ApiOperation(
			value = "Delete the building tree",
			notes = "Delete the building (indentified by id) from the rule database including all the linked rules")
	public
	@ResponseBody
	ResponseEntity<Void> deleteBuildingTree(@ApiParam("ID of the building") @PathVariable Long id) throws IllegalAccessException, BuildingDatabaseException {
		buildingUtils.deleteBuildingTree(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = "/building/{id}")
	@ApiOperation(
			value = "Get the building tree",
			notes = "Get the building (indentified by id) from the rule databas")
	public
	@ResponseBody
	ResponseEntity<Area> getBuildingTree(@ApiParam("ID of the building") @PathVariable Long id) throws IllegalAccessException, BuildingDatabaseException {
		School school = rulesLoader.loadSchools().get(id);
		return ResponseEntity.ok(school);
	}

	@GetMapping(value = "/buildings")
	@ApiOperation(value = "Get all buildings stored in the rule database", responseContainer = "List")
	public
	@ResponseBody
	ResponseEntity<List<Area>> getSchools() {
		return ResponseEntity.ok(Lists.newArrayList(rulesLoader.loadSchools().values()));
	}

	@GetMapping(value = "/building/{bid}/areas")
	@ApiOperation(value = "Get all subareas of an area/building stored in the rule database")
	public ResponseEntity<List<Area>> getAreas(@ApiParam("ID of the building") @PathVariable Long bid) {
		List<Area> subAreas = buildingUtils.getSubAreas(bid);
		return ResponseEntity.status(HttpStatus.OK).body(subAreas);
	}

	//------ RELOAD ------
	/*
	@ResponseBody
	@ApiOperation(value = "Force reloaing of the rules tree of the building identified by aid", response = School.class, responseContainer = "List")
	@RequestMapping(value = "/building/reload/{aid}", method = RequestMethod.GET)
	public ResponseEntity<String> reloadSchoolTree(@PathVariable Long aid) throws RulesLoaderException {
		if (!rulesLoader.reloadSchool(aid)) {
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
	*/


}
