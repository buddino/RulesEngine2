package it.cnit.gaia.rulesengine.api;

import io.swagger.annotations.*;
import it.cnit.gaia.rulesengine.api.dto.CompositeDTO;
import it.cnit.gaia.rulesengine.api.dto.ConditionDTO;
import it.cnit.gaia.rulesengine.api.dto.ErrorResponse;
import it.cnit.gaia.rulesengine.api.dto.RuleDTO;
import it.cnit.gaia.rulesengine.api.exception.GaiaRuleException;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.rules.AllCompositeRule;
import it.cnit.gaia.rulesengine.rules.AnyCompositeRule;
import it.cnit.gaia.rulesengine.service.RuleDatabaseService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "Rules API",
		description = "API for managing the rules",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class RulesController {

	private Logger LOGGER = Logger.getRootLogger();

	@Autowired
	private RuleDatabaseService ruleDatabaseService;

	@ApiOperation(value = "GET rules of area",
			notes = "Get all the rules associated to the area identified by {id}",
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


	@ApiOperation(value = "DELETE rule", notes = "Delete the rule identified by {rid} only if this has been created by the user")
	@DeleteMapping(value = "/rules/{rid}")
	@ResponseBody
	public ResponseEntity<String> deleteRule(@ApiParam("Identifier of the rule") @PathVariable String rid) throws GaiaRuleException {
		ruleDatabaseService.deleteRule(rid);
		return ResponseEntity.noContent().build();
	}

	@ApiOperation(value = "EDIT the rule", notes = "Modify the rule identified by {rid} according to the object passed in the body")
	@PutMapping(value = "/rules/{rid}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RuleDTO> editRule(@ApiParam("Identifier of the rule") @PathVariable String rid, @RequestBody RuleDTO ruleDTO) throws Exception {
		ruleDatabaseService.editCustomRule(rid, ruleDTO);
		return ResponseEntity.ok(ruleDTO);
	}

	@ApiOperation(value = "GET rule", notes = "Get the rule identified by {rid}")
	@GetMapping(value = "/rules/{rid}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RuleDTO> getRule(@ApiParam("Identifier of the rule") @PathVariable String rid) throws GaiaRuleException {
		RuleDTO rule = ruleDatabaseService.getRuleFromDb(rid);
		return ResponseEntity.ok(rule);
	}

	@ApiOperation(value = "EVALUATE rule's condition", notes = "Evaluate the condition of a rule")
	@GetMapping(value = "/rules/{rid}/condition", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<ConditionDTO> evalCondition(@ApiParam("Identifier of the rule") @PathVariable String rid) {
		GaiaRule rule = ruleDatabaseService.getRuleFromRuntime(rid);
		if (rule == null)
			return ResponseEntity.notFound().build();
		ConditionDTO condition = new ConditionDTO();
		condition.setCondition(rule.condition());
		Map<String, Object> allFields = rule.getAllFields();
		RuleDTO ruleDTO = new RuleDTO();
		ruleDTO.setRid(rid).setClazz(rule.getClass().getSimpleName()).setFields(allFields);
		condition.setRule(ruleDTO);
		return ResponseEntity.ok(condition);
	}

	@ApiOperation(value = "FIRE rule", notes = "Force fire for the specified rule. There is no guarantee the action will be execute, it depends on the conditions.")
	@GetMapping(value = "/rules/{rid}/fire", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RuleDTO> triggerRule(@ApiParam("Identifier of the rule") @PathVariable String rid) {
		GaiaRule rule = ruleDatabaseService.getRuleFromRuntime(rid);
		if (rule == null)
			return ResponseEntity.notFound().build();
		rule.fire();
		Map<String, Object> allFields = rule.getAllFields();
		RuleDTO ruleDTO = new RuleDTO();
		ruleDTO.setRid(rid).setClazz(rule.getClass().getSimpleName()).setFields(allFields);
		return ResponseEntity.ok(ruleDTO);
	}


	@ApiOperation(value = "ADD a rule to area", notes = "Add a custom rule according to the object passed in the body")
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

	@ApiOperation(value = "TODO", notes = "TODO")
	@PostMapping(value = "/area/{aid}/rules/composite", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RuleDTO> addComposite(
			@ApiParam("ID of the area")
			@PathVariable Long aid,
			@ApiParam(value = "JSON Object describing the composition")
			@RequestBody CompositeDTO compositeDTO) throws Exception {

		//TODO Move into service
		RuleDTO composite = new RuleDTO();
		Map<String,Object> compositeFields = new HashMap<>();

		if(compositeDTO.getOperator().equals("OR")){
			composite.setClazz(AnyCompositeRule.class.getSimpleName());
		}
		else if(compositeDTO.getOperator().equals("AND")){
			composite.setClazz(AllCompositeRule.class.getSimpleName());
		}
		else {
			throw new GaiaRuleException("Valid logical operators ar OR and AND");
		}
		compositeFields.put("name",compositeDTO.getName());
		compositeFields.put("suggestion",compositeDTO.getSuggestion());
		composite.setFields(compositeFields);
		composite = ruleDatabaseService.addCustomRuleToArea(aid, composite);
		for (RuleDTO rule : compositeDTO.getRules()) {
			ruleDatabaseService.addCustomRuleToComposite(composite.getRid(),rule);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(composite);
	}

	@ApiOperation(value = "ADD a rule to composite", notes = "Add a custom rule to a compsoite rule according to the object passed in the body. The rid MUST identify a CompositeRule")
	@PostMapping(value = "/rules/{rid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RuleDTO> addRuleToComposite(
			@ApiParam("ID of the composite rule")
			@PathVariable String rid,
			@ApiParam(value = "JSON Object describing the rule")
			@RequestBody RuleDTO ruleDTO) throws Exception {
		ruleDTO = ruleDatabaseService.addCustomRuleToComposite(rid, ruleDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(ruleDTO);
	}

	@PutMapping(value = "/building/{id}/fire")
	@ApiOperation(value = "FIRE rules for building", notes = "Force firing of the rules for a building. There is no guarantee the action will be execute, it depends on the conditions.")
	public ResponseEntity<Void> triggerRuleOfSchool(@ApiParam("Building id") @PathVariable Long id) throws Exception {
		School school = ruleDatabaseService.getSchool(id);
		school.fire();
		return ResponseEntity.ok(null);
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
