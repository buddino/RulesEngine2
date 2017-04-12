package it.cnit.gaia.rulesengine.api;

import it.cnit.gaia.rulesengine.api.request.CustomRuleException;
import it.cnit.gaia.rulesengine.api.request.ErrorResponse;
import it.cnit.gaia.rulesengine.utils.DatabaseSchemaService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class SchemaController {
	private Logger LOGGER = Logger.getRootLogger();

	@Autowired
	private DatabaseSchemaService dbService;


	@RequestMapping(value = "/schema/{classname}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> setDefault(@PathVariable String classname, @RequestBody Map<String, String> defaults) {
		for (Map.Entry<String, String> e : defaults.entrySet()) {
			dbService.setDefaultForPropertyInClass(classname, e.getKey(), e.getValue());
		}
		return ResponseEntity.ok(dbService.getDefaultForClass(classname));
	}

	@RequestMapping(value = "/schema/{classname}/default", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> getDefault(@PathVariable String classname) {
		return ResponseEntity.ok(dbService.getDefaultForClass(classname));
	}

	@RequestMapping(value = "/schema/{classname}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> getClassSchema(@PathVariable String classname) {
		return ResponseEntity.ok(dbService.getClassSchema(classname));
	}

	@RequestMapping(value = "/schema/{classname}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> addJavaClassToSchema(@PathVariable String classname) {
		dbService.addClassToSchema(classname);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(dbService.getClassSchema(classname));
	}

	@ExceptionHandler(CustomRuleException.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(Exception e) {
		ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}


}
