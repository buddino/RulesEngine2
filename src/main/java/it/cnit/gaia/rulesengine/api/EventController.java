package it.cnit.gaia.rulesengine.api;

import com.orientechnologies.orient.core.record.impl.ODocument;
import it.cnit.gaia.rulesengine.service.EventService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class EventController {

	@Autowired
	private EventService eventService;

	@GetMapping(value = "/events" , produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getEvents(
			@RequestParam(defaultValue = "", required = false) String ruleClass,
			@RequestParam(defaultValue = "", required = false) String ruleId,
			@RequestParam(defaultValue = "10", required = false) Integer limit) {
		if (!ruleId.equals("")) {
			return ResponseEntity.ok(eventsToJson(eventService.getEventsForRule(ruleId, limit)));
		}
		if (!ruleClass.equals("")) {
			return  ResponseEntity.ok(eventsToJson(eventService.getEventsByRuleClass(ruleClass, limit)));
		}
		return  ResponseEntity.ok(eventsToJson(eventService.getLatestEvents(limit)));
	}

	@GetMapping("/building/{schoolId}/events")
	public String getEventsForSchool(
			@PathVariable String schoolId,
			@RequestParam(required = false) Long from,
			@RequestParam(required = false) Long to,
			@RequestParam(defaultValue = "10", required = false) Integer limit) {
		if (from == null || to == null)
			return eventsToJson(eventService.getEventsForSchool(schoolId, limit));
		else
			return eventsToJson(eventService.getEventsForSchoolTimeRange(schoolId, from, to, limit));
	}


	@NotNull
	private String eventsToJson(List<ODocument> list) {
		if (list == null)
			return "[]";
		return list.stream()
				.map(d -> d.toJSON("rid,fetchPlan:rule:1"))
				.collect(Collectors.joining(",", "[", "]"));
	}


}
