package it.cnit.gaia.rulesengine.api;

import com.orientechnologies.orient.core.record.impl.ODocument;
import io.swagger.annotations.*;
import it.cnit.gaia.rulesengine.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "Event log",
		authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "read")})},
		produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class EventController {

	@Autowired
	private EventService eventService;

	@ApiOperation(value = "Get events", notes = "This API retrieves the events logged fot all the buildings")
	@GetMapping(value = "/events" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getEvents(
			@ApiParam(value = "Name of the rule class which generated the events", example = "PowerFactor") @RequestParam(defaultValue = "", required = false) String ruleClass,
			@ApiParam(value = "Id of the rule which generated the events", example = "25:5") @RequestParam(defaultValue = "", required = false) String ruleId,
			@ApiParam(value = "Limit for ther retrieved events",defaultValue = "10", example = "25") @RequestParam(defaultValue = "10", required = false) Integer limit) {
		if (!ruleId.equals("")) {
			return ResponseEntity.ok(eventsToJson(eventService.getEventsForRule(ruleId, limit)));
		}
		if (!ruleClass.equals("")) {
			return  ResponseEntity.ok(eventsToJson(eventService.getEventsByRuleClass(ruleClass, limit)));
		}
		return  ResponseEntity.ok(eventsToJson(eventService.getLatestEvents(limit)));
	}

	@ApiOperation(value = "Get events for a building", notes = "This API retrieves the events logged fot all the buildings")
	@GetMapping("/building/{bid}/events")
	public String getEventsForSchool(
			@ApiParam(value = "The ID of the building the events belong", required = true, example = "153453") @PathVariable String schoolId,
			@ApiParam(value = "From time (timestamp)", example = "1492521504000") @RequestParam(required = false) Long from,
			@ApiParam(value = "To time (timestamp)", example = "1492511304000") @RequestParam(required = false) Long to,
			@ApiParam(value = "Limit for ther retrieved events", example = "10") @RequestParam(defaultValue = "10", required = false) Integer limit) {
		if (from == null || to == null)
			return eventsToJson(eventService.getEventsForSchool(schoolId, limit));
		else
			return eventsToJson(eventService.getEventsForSchoolTimeRange(schoolId, from, to, limit));
	}


	private String eventsToJson(List<ODocument> list) {
		if (list == null)
			return "[]";
		return list.stream()
				.map(d -> d.toJSON("rid,fetchPlan:rule:1"))
				.collect(Collectors.joining(",", "[", "]"));
	}


}
