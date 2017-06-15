package it.cnit.gaia.rulesengine.api;

import io.swagger.annotations.*;
import it.cnit.gaia.rulesengine.api.request.ErrorResponse;
import it.cnit.gaia.rulesengine.api.request.GaiaRuleException;
import it.cnit.gaia.rulesengine.api.request.RuleDTO;
import it.cnit.gaia.rulesengine.service.RuleDatabaseService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "Rules",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class RulesController {

	private Logger LOGGER = Logger.getRootLogger();

	@Autowired
	private RuleDatabaseService ruleDatabaseService;


	@ApiOperation(value = "Get all the rules associated to the area identified by {id}",
			responseContainer = "List")
	@GetMapping(value = "/area/{aid}/rules", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<RuleDTO>> getRuleOfArea(
			@ApiParam("ID of the area")
			@PathVariable Long aid,
			@ApiParam("If true also all the rules beloging to children areas of the area passed as parameter are shown")
			@RequestParam(required = false, defaultValue = "false") Boolean traverse) throws GaiaRuleException {
		List<RuleDTO> rulesOfArea = ruleDatabaseService.getRulesForArea(aid, traverse);
		return ResponseEntity.ok(rulesOfArea);
	}

	@ApiOperation(value = "Delete the rule identified by {rid} only if this has been created by the user")
	@DeleteMapping(value = "/rules/{rid}")
	@ResponseBody
	public ResponseEntity<String> deleteRule(@ApiParam("Identifier of the rule") @PathVariable String rid) throws GaiaRuleException {
		ruleDatabaseService.deleteRule(rid);
		return ResponseEntity.noContent().build();
	}

	@ApiOperation(value = "Modify the rule identified by {rid} according to the object passed in the body")
	@PutMapping(value = "/rules/{rid}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RuleDTO> editRule(@ApiParam("Identifier of the rule") @PathVariable String rid, @RequestBody RuleDTO ruleDTO) throws Exception {
		ruleDatabaseService.editCustomRule(rid, ruleDTO);
		return ResponseEntity.ok(ruleDTO);
	}

	@ApiOperation(value = "Get the rule identified by {rid}")
	@GetMapping(value = "/rules/{rid}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RuleDTO> getRule(@ApiParam("Identifier of the rule") @PathVariable String rid) throws Exception {
		RuleDTO rule = ruleDatabaseService.getRule(rid);
		return ResponseEntity.ok(rule);
	}


	@ApiOperation(value = "Add a custom rule according to the object passed in the body")
	@PostMapping(value = "/area/{aid}/rules", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RuleDTO> addRuleToArea(
			@ApiParam("ID of the area")
			@PathVariable Long aid,
			@ApiParam(value = "JSON Object describing the rule")
			@RequestBody RuleDTO ruleDTO) throws Exception {
		ruleDTO = ruleDatabaseService.addCustomRuleToArea(aid, ruleDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(ruleDTO);
	}

	@ExceptionHandler(GaiaRuleException.class)
	public ResponseEntity<ErrorResponse> exceptionHandler(GaiaRuleException e) {
		if (e.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
			LOGGER.error(e);
		}
		ErrorResponse error = new ErrorResponse(e.getStatus().value(), e.getMessage());
		ResponseEntity responseEntity = new ResponseEntity(error, null, e.getStatus());
		return responseEntity;
	}


}
