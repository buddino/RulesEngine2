package it.cnit.gaia.rulesengine.api;

import io.swagger.annotations.*;
import it.cnit.gaia.rulesengine.api.dto.ErrorResponse;
import it.cnit.gaia.rulesengine.api.exception.GaiaRuleException;
import it.cnit.gaia.rulesengine.service.DatabaseSchemaService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "Schema",
		description = "API for retrieving information about rule classes",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class SchemaController {
	private Logger LOGGER = Logger.getRootLogger();

	@Autowired
	private DatabaseSchemaService dbService;


	@ApiOperation(
			value = "GET rule fields",
			notes = "Get the rule class definition. <br> Example: { 'power_threshold': {'regex': null,'default': null,'mandatory': true}, ... }")
	@GetMapping(value = "schema/{classname}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> getClassSchema(@PathVariable String classname) {
		return ResponseEntity.ok(dbService.getClassSchema(classname));
	}

	@ApiOperation(
			value = "GET all the rule classes",
			notes = "TODO")
	@GetMapping(value = "classes", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<String>> getRuleClasses() {
		return ResponseEntity.ok(dbService.getRuleClasses());
	}





	@ApiOperation(
			value = "CREATE rule class in schema",
			notes = "Create the appropriate class in the database schema corresponding to the java class whose name has been passed as path variable<br>" +
					"Example: { 'power_threshold': {'regex': null,'default': null,'mandatory': true}, ... }")
	@PostMapping(value = "schema/{classname}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> addJavaClassToSchema(
			@ApiParam(value = "The name name of the java class to be added to the schema of the rule db (must be equal to the Java class name, without the full package path, only the classname)",
					example = "PowerFactor", required = true)
			@PathVariable String classname) {
		dbService.addClassToSchema(classname);
		return ResponseEntity.status(HttpStatus.CREATED)
							 .body(dbService.getClassSchema(classname));
	}





	@ExceptionHandler(GaiaRuleException.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(Exception e) {
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}


}
