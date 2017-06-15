package it.cnit.gaia.rulesengine.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Api(tags = "Status",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
public class StatusController {

	@Autowired
	private MeasurementRepository measurementRepository;
	/*
	@GetMapping(value = "/status")
	public ResponseEntity<String> getStatus(HttpServletRequest request){

		if( request.isUserInRole("ADMIN") ){
			return ResponseEntity.ok("Ok");
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(request.getRemoteUser());
	}
	*/

	@GetMapping("/metermap")
	@ApiOperation("Returns the conversion map from URIs to numerical Resource IDs")
	public
	@ResponseBody
	ResponseEntity<Map<String, Long>> getUriMapping() {
		return ResponseEntity.ok(measurementRepository.getMeterMap());
	}
}
