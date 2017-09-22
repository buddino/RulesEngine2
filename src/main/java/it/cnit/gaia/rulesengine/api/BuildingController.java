package it.cnit.gaia.rulesengine.api;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import io.swagger.annotations.*;
import io.swagger.sparks.ApiException;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.api.dto.ErrorResponse;
import it.cnit.gaia.rulesengine.api.exception.GaiaRuleException;
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

import java.util.ArrayList;
import java.util.List;

@RestController
@Api(tags = "Building API",
		description = "API for managing the building trees replicated into the rules database. This API is used to maintain the sync with the building db.",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class BuildingController {

	private Logger LOGGER = Logger.getRootLogger();

	@Autowired
	private RulesLoader rulesLoader;
	@Autowired
	private BuildingUtils buildingUtils;

	/*@ExceptionHandler(BuildingDatabaseException.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(Exception e) {
		ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), e.getMessage());
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}*/

	@PutMapping(value = "building/{id}")
	@ApiOperation(
			value = "IMPORT building structure",
			notes = "Import the building (indentified by id) from the building database replicating its structure")
	public
	@ResponseBody
	ResponseEntity<Void> buildTreeFromBuildingDB(@ApiParam("ID of the building") @PathVariable Long id) throws IllegalAccessException, BuildingDatabaseException, ApiException {
		OrientVertex vertex = buildingUtils.buildTreeFromBuildingDB(id);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping(value = "building/{id}")
	@ApiOperation(
			value = "DELETE building structure",
			notes = "Delete the building (indentified by id) from the rule database including all the linked rules")
	public
	@ResponseBody
	ResponseEntity<Void> deleteBuildingTree(@ApiParam("ID of the building") @PathVariable Long id) throws IllegalAccessException, BuildingDatabaseException {
		buildingUtils.deleteBuildingTreeIncludingTheRules(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(value = "building/{id}")
	@ApiOperation(
			value = "GET building information",
			notes = "Get the building (indentified by id) from the rule database")
	public
	@ResponseBody
	ResponseEntity<Area> getBuildingTree(@ApiParam("ID of the building") @PathVariable Long id) throws IllegalAccessException, BuildingDatabaseException {
		School school = rulesLoader.getSchools().get(id);
		if(school == null)
			return ResponseEntity.notFound().build();
		return ResponseEntity.ok(school);
	}

	@GetMapping(value = "buildings")
	@ApiOperation(value = "GET all the buildings", notes = "Get all the buildings stored in the rule database", responseContainer = "List")
	public
	@ResponseBody
	ResponseEntity<List<Area>> getSchools() {
		if(rulesLoader.getSchools()!=null)
			return ResponseEntity.ok(Lists.newArrayList(rulesLoader.getSchools().values()));
		else
			return ResponseEntity.ok(new ArrayList<>());
	}

	@GetMapping(value = "building/{bid}/areas")
	@ApiOperation(value = "GET subareas",
	notes = "Get all subareas of an area/building stored in the rule database")
	public ResponseEntity<List<Area>> getAreas(@ApiParam("ID of the building") @PathVariable Long bid) {
		List<Area> subAreas = buildingUtils.getSubAreas(bid);
		return ResponseEntity.status(HttpStatus.OK).body(subAreas);
	}

	@ExceptionHandler(BuildingDatabaseException.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(GaiaRuleException e) {
		if (e.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
			LOGGER.error(e);
		}
		ErrorResponse error = new ErrorResponse(e.getStatus().value(), e.getMessage());
		ResponseEntity responseEntity = new ResponseEntity(error, null, e.getStatus());
		return responseEntity;
	}
}
