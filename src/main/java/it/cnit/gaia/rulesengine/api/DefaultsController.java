package it.cnit.gaia.rulesengine.api;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import io.swagger.annotations.*;
import it.cnit.gaia.rulesengine.api.dto.DefaultsDTO;
import it.cnit.gaia.rulesengine.api.dto.ErrorResponse;
import it.cnit.gaia.rulesengine.api.exception.GaiaRuleException;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.service.RuleDatabaseService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@RestController
@Consumes({MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
@Api(tags = "Defaults API",
		description = "API for managing default values for the rule classes",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class DefaultsController {

	private Logger LOGGER = Logger.getRootLogger();

	@Autowired
	private OrientGraphFactory ogf;

	@Autowired
	private RuleDatabaseService ruleDatabaseService;


	//Add a default
	@ApiOperation(value = "ADD defaults", notes = "Add the default values for the specified rule class")
	@PostMapping(value = "rules/{classname}/default", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<ODocument> addDefaultForClass(
			@ApiParam(value = "The name of the rule class", example = "PowerFactor", required = true)
			@PathVariable String classname, @RequestBody DefaultsDTO defaultsDTO) throws GaiaRuleException {
		//TODO Move out
		ODocument result = ruleDatabaseService.addDefault(classname, defaultsDTO);
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
	public ResponseEntity<ODocument> editDefaultForClass(
			@ApiParam(value = "The name of the rule class", example = "PowerFactor", required = true)
			@PathVariable String classname, @RequestBody DefaultsDTO defaultsDTO) throws GaiaRuleException {
		ODocument result = ruleDatabaseService.editDefault(classname, defaultsDTO);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	@ApiOperation(value = "GET defaults", notes = "Retrieve the default values for the specified rule class")
	@GetMapping(value = "rules/{classname}/default")
	@ResponseBody
	public ResponseEntity<DefaultsDTO> getDefaultForClass(
			@ApiParam(value = "The name of the rule class", example = "PowerFactor", required = true)
			@PathVariable String classname, @RequestParam(required = false,defaultValue = "false") Boolean force) throws GaiaRuleException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		DefaultsDTO defaults = ruleDatabaseService.getDefault(classname);
		if (defaults == null) {
			if(force)
				defaults = buildAndCreatDefaults(classname);
			else
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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

	//Patch a default


	//Put a default

	private DefaultsDTO buildAndCreatDefaults(String classname) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		DefaultsDTO defaultsDTO = new DefaultsDTO();
		Class<?> aClass = Class.forName(RulesLoader.rulesPackage + "." + classname);
		Object c = aClass.newInstance();
		Field[] classFields = aClass.getFields();
		Map<String,Map<String,Object>> fields = new HashMap<>();
		for (Field f : classFields) {
			if (f.isAnnotationPresent(LoadMe.class)) {
				LoadMe annotation = f.getAnnotation(LoadMe.class);
				Map<String,Object> field = new HashMap<>();
				field.put("value",f.get(c));
				if(annotation.required())
					field.put("required",true);
				else
					field.put("required",false);
				fields.put(f.getName(),field);
			}
		}
		defaultsDTO.setFields(fields);
		return defaultsDTO;
	}
}
