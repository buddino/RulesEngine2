package it.cnit.gaia.rulesengine.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.sparks.model.SingleResourceMeasurementAPIModel;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import it.cnit.gaia.rulesengine.service.MetadataService;
import it.cnit.gaia.rulesengine.service.RuleDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

@RestController
@RequestMapping("utils")
@Api(tags = "Utility API",
		description = "API for debugging or retrieve the status of the module",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class UtilityController {

	@Autowired
	MeasurementRepository measurementRepository;
	@Autowired
	private RuleDatabaseService ruleDatabaseService;
	@Autowired
	private MetadataService metadataService;


	@ApiOperation(notes = "Outputs the latest log of the recommendation engine", value = "GET log")
	@GetMapping(value = "log", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> getLog() throws FileNotFoundException {
		FileReader fr = new FileReader("./RulesEngine.log");
		BufferedReader br = new BufferedReader(fr);
		StringBuffer bf = new StringBuffer();
		br.lines().forEach(x -> bf.append(x).append("\r\n"));
		return ResponseEntity.ok(bf.toString());
	}

	@GetMapping("metermap")
	@ApiOperation(notes = "Returns the conversion map from URIs to numerical Resource IDs", value = "GET Uri2Id Map")
	public
	@ResponseBody
	ResponseEntity<Map<String, Long>> getUriMapping() {
		return ResponseEntity.ok(measurementRepository.getMeterMap());
	}

	@GetMapping("measurements/latest")
	@ApiOperation(notes = "Returns the conversion map from URIs to numerical Resource IDs", value = "GET Uri2Id Map")
	public
	@ResponseBody
	ResponseEntity<Map<String, SingleResourceMeasurementAPIModel>> getLatestMeasurements() {
		return ResponseEntity.ok(measurementRepository.getLatestReadings());
	}

	@PutMapping("schedules/update")
	@ApiOperation(notes = "Sync the schedules / calendar with the building database", value = "FORCE SYNC of sites metadata")
	public @ResponseBody
	ResponseEntity updateSchedules() {
		metadataService.updateAll();
		return ResponseEntity.noContent().build();
	}

	@GetMapping("rules/update")
	@ApiOperation(value = "RELOAD all rules", notes = "Force rules reloading for all buildings.")
	public ResponseEntity<Void> reloadRules(@RequestParam(defaultValue = "false", required = false) Boolean reloadnow) {
		ruleDatabaseService.reloadAllSchools(reloadnow);
		return ResponseEntity.noContent().build();
	}



}
