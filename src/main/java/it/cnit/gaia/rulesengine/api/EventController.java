package it.cnit.gaia.rulesengine.api;

import io.swagger.annotations.*;
import it.cnit.gaia.rulesengine.api.dto.EventDTO;
import it.cnit.gaia.rulesengine.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "Event log",
		description = "API for retrieving the logged events",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class EventController {

	@Autowired
	private EventService eventService;

	@ApiOperation(value = "GET events", notes = "This API retrieves the events logged for all the buildings")
	@GetMapping(value = "/events", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<EventDTO>> getEvents(
			@ApiParam(value = "Name of the rule class which generated the events", example = "PowerFactor") @RequestParam(defaultValue = "", required = false) String ruleClass,
			@ApiParam(value = "Id of the rule which generated the events", example = "25:5") @RequestParam(defaultValue = "", required = false) String ruleId,
			@ApiParam(value = "Limit the number of retrieved events", defaultValue = "10", example = "25") @RequestParam(defaultValue = "10", required = false) Integer limit) {
		if (!ruleId.equals("")) {
			return ResponseEntity.ok(eventService.getEventsForRule(ruleId, limit));
		}
		if (!ruleClass.equals("")) {
			return ResponseEntity.ok(eventService.getEventsByRuleClass(ruleClass, limit));
		}
		return ResponseEntity.ok(eventService.getLatestEvents(limit));
	}

	@ApiOperation(value = "GET events for building", notes = "This API retrieves the events logged for the specified building")
	@GetMapping("/building/{bid}/events")
	public ResponseEntity<List<EventDTO>> getEventsForSchool(
			@ApiParam(value = "The ID of the building the events belong", required = true, example = "153453") @PathVariable Long bid,
			@ApiParam(value = "From time (timestamp)", example = "1492521504000") @RequestParam(required = false) Long from,
			@ApiParam(value = "To time (timestamp)", example = "1492511304000") @RequestParam(required = false) Long to,
			@ApiParam(value = "Limit the number of retrieved events", example = "10") @RequestParam(defaultValue = "10", required = false) Integer limit) {
		if (from == null || to == null)
			return ResponseEntity.ok(eventService.getEventsForSchool(bid, limit));
		else
			return ResponseEntity.ok(eventService.getEventsForSchoolTimeRange(bid, from, to, limit));
	}
}
