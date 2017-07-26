package it.cnit.gaia.rulesengine.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import it.cnit.gaia.rulesengine.service.MetadataServiceOld;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

@RestController
@RequestMapping("/utils")
@Api(tags = "Utility API",
		description = "API for debugging or retrieve the status of the module",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class UtilityController {

	@Autowired
	MeasurementRepository measurementRepository;
	@Autowired
	private RulesLoader rulesLoader;
	@Autowired
	private MetadataServiceOld metadataService;


	@ApiOperation(notes = "Outputs the latest log of the recommendation engine", value = "GET log")
	@GetMapping(value = "/log", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> getLog() throws FileNotFoundException {
		FileReader fr = new FileReader("./RulesEngine.log");
		BufferedReader br = new BufferedReader(fr);
		StringBuffer bf = new StringBuffer();
		br.lines().forEach(x -> bf.append(x).append("\r\n"));
		return ResponseEntity.ok(bf.toString());
	}

	@GetMapping("/metermap")
	@ApiOperation(notes = "Returns the conversion map from URIs to numerical Resource IDs", value = "GET Uri2Id Map")
	public
	@ResponseBody
	ResponseEntity<Map<String, Long>> getUriMapping() {
		return ResponseEntity.ok(measurementRepository.getMeterMap());
	}

	@PutMapping("/schedules/update")
	@ApiOperation(notes = "Sync the schedules / calendar with the building database", value = "FORCE SYNC of sites metadata")
	public @ResponseBody
	ResponseEntity updateSchedules() {
		metadataService.updateAll();
		return ResponseEntity.noContent().build();
	}



}
