package it.cnit.gaia.rulesengine.api;

import io.swagger.annotations.*;
import it.cnit.gaia.rulesengine.api.dto.DefaultsDTO;
import it.cnit.gaia.rulesengine.api.dto.ErrorResponse;
import it.cnit.gaia.rulesengine.api.dto.RuleDTO;
import it.cnit.gaia.rulesengine.api.exception.GaiaRuleException;
import it.cnit.gaia.rulesengine.service.RuleCreationHelper;
import it.cnit.gaia.rulesengine.service.RuleDatabaseService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.ws.rs.Consumes;
import java.util.Map;

@RestController
@ApiIgnore
@Consumes({MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
@Api(tags = "Defaults API",
		description = "API for managing default values for the rule classes",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class DefaultsController {

	private Logger LOGGER = Logger.getRootLogger();

	@Autowired
	private RuleDatabaseService ruleDatabaseService;
	@Autowired
	private RuleCreationHelper helper;


	//Add a default
	@ApiOperation(value = "ADD defaults", notes = "Add the default values for the specified rule class")
	@PostMapping(value = "rules/{classname}/default", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<DefaultsDTO> addDefaultForClass(
			@ApiParam(value = "The name of the rule class", example = "PowerFactor", required = true)
			@PathVariable String classname, @RequestBody DefaultsDTO defaultsDTO) throws GaiaRuleException {
		//TODO Move out
		DefaultsDTO result = ruleDatabaseService.addDefault(classname, defaultsDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	//Delete default
	@ApiOperation(value = "DELETE defaults", notes = "Delete the default values for the specified rule class")
	@DeleteMapping(value = "rules/{classname}/default")
	@ResponseBody
	public ResponseEntity deleteDefaultForClass(
			@ApiParam(value = "The name of the rule class", example = "PowerFactor", required = true)
			@PathVariable String classname) throws GaiaRuleException {
		ruleDatabaseService.deleteDefault(classname);
		return ResponseEntity.noContent().build();
	}

	@ApiOperation(value = "EDIT defaults", notes = "Edit the default values for the specified rule class")
	@PutMapping(value = "rules/{classname}/default")
	@ResponseBody
	public ResponseEntity<DefaultsDTO> editDefaultForClass(
			@ApiParam(value = "The name of the rule class", example = "PowerFactor", required = true)
			@PathVariable String classname, @RequestBody DefaultsDTO defaultsDTO) throws GaiaRuleException {
		DefaultsDTO result = ruleDatabaseService.editDefault(classname, defaultsDTO);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@ApiOperation(value = "GET defaults", notes = "Retrieve the default values for the specified rule class")
	@GetMapping(value = "rules/{classname}/default")
	@ResponseBody
	public ResponseEntity<DefaultsDTO> getDefaultForClass(
			@ApiParam(value = "The name of the rule class", example = "PowerFactor", required = true)
			@PathVariable String classname, @RequestParam(required = false, defaultValue = "false") Boolean force) throws GaiaRuleException, ClassNotFoundException, IllegalAccessException, InstantiationException {

		DefaultsDTO defaults = ruleDatabaseService.getDefault(classname);
		DefaultsDTO hardcoded = helper.buildAndCreateDefaults(classname);

		if (defaults == null) {
			if (force)
				return ResponseEntity.status(HttpStatus.OK).body(hardcoded);
			else
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		for (String key : hardcoded.getFields().keySet()) {
			defaults.getFields().putIfAbsent(key, hardcoded.getFields().get(key));
		}

		return ResponseEntity.status(HttpStatus.OK).body(defaults);
	}

	@ApiOperation(value = "GET defaults", notes = "Retrieve the default values for the specified rule class")
	@GetMapping(value = "rules/{classname}/default/fields")
	@ResponseBody
	public ResponseEntity<Map<String, Map<String, Object>>> getDefaultFieldsForClass(
			@ApiParam(value = "The name of the rule class", example = "PowerFactor", required = true)
			@PathVariable String classname) throws GaiaRuleException {
		DefaultsDTO defaults = ruleDatabaseService.getDefault(classname);
		return ResponseEntity.status(HttpStatus.OK).body(defaults.getFields());
	}

	@ApiOperation(value = "GET defaults", notes = "Retrieve the default values for the specified rule class")
	@GetMapping(value = "rules/{classname}/default/suggestion")
	@ResponseBody
	public ResponseEntity<Map<String, String>> getDefaultSuggestionForClass(
			@ApiParam(value = "The name of the rule class", example = "PowerFactor", required = true)
			@PathVariable String classname) throws GaiaRuleException {
		DefaultsDTO defaults = ruleDatabaseService.getDefault(classname);
		return ResponseEntity.status(HttpStatus.OK).body(defaults.getSuggestion());
	}

	//Exception handling
	@ExceptionHandler(GaiaRuleException.class)
	public ResponseEntity<ErrorResponse> gaiaexceptionHandler(GaiaRuleException e) {
		if (e.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
			LOGGER.error(e);
		}
		ErrorResponse error = new ErrorResponse(e.getStatus().value(), e.getMessage());
		ResponseEntity responseEntity = new ResponseEntity(error, null, e.getStatus());
		return responseEntity;
	}

	@ApiOperation(value = "GET a rule suggestion", notes = "GET default values before creating a rule")
	@GetMapping(value = "area/{aid}/{classname}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RuleDTO> suggestFields(
			@ApiParam("ID of the area")
			@PathVariable Long aid,
			@ApiParam(value = "Rule classname")
			@PathVariable String classname,
			@ApiParam(value = "Language (e.g.,it,en,el...)")
			@RequestParam(defaultValue = "en", required = false) String lang,
			@RequestParam(defaultValue = "false") @ApiParam(value = "Outputs also the hardcoded values if no default is set") Boolean hardcoded) throws Exception {
		RuleDTO suggestion = helper.getSuggestion(aid, classname, lang, hardcoded);
		if (suggestion == null)
			return ResponseEntity.notFound().build();
		return ResponseEntity.ok(suggestion);
	}

}
