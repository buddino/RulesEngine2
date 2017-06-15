package it.cnit.gaia.rulesengine.api;

import io.swagger.annotations.*;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

@RestController
@RequestMapping("/debug")
@Api(tags = "Debug",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class DebugController {

	@Autowired
	private RulesLoader rulesLoader;

	@Autowired
	MeasurementRepository measurementRepository;

	@PutMapping(value = "/trigger/{id}")
	@ApiOperation(value = "Force triggering of the rules for a building")
	public ResponseEntity<?> triggerRuleOfSchool(@ApiParam("Building id") @PathVariable Long id) throws Exception {
		School school = rulesLoader.getSchool(id);
		school.fire();
		return ResponseEntity.ok(null);
	}
	@ApiOperation(value = "Outputs the latest log of the recommendation engine")
	@GetMapping(value = "/log", produces= MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> getLog() throws FileNotFoundException {
		FileReader fr = new FileReader("./RulesEngine.log");
		BufferedReader  br = new BufferedReader(fr);
		StringBuffer bf = new StringBuffer();
		br.lines().forEach(x->bf.append(x).append("\r\n"));
		return ResponseEntity.ok(bf.toString());
	}

	@GetMapping("/metermap")
	@ApiOperation("Returns the conversion map from URIs to numerical Resource IDs")
	public
	@ResponseBody
	ResponseEntity<Map<String, Long>> getUriMapping() {
		return ResponseEntity.ok(measurementRepository.getMeterMap());
	}



}
