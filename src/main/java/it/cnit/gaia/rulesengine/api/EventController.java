package it.cnit.gaia.rulesengine.api;

import com.orientechnologies.orient.core.record.impl.ODocument;
import it.cnit.gaia.rulesengine.event.EventService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class EventController {

	@Autowired
	private EventService eventService;

	@RequestMapping("/events")
	public String getEvents(
			@RequestParam(defaultValue = "", required = false) String ruleClass,
			@RequestParam(defaultValue = "", required = false) String ruleId,
			@RequestParam(defaultValue = "false", required = false) Boolean prefetch,
			@RequestParam(defaultValue = "10", required = false) Integer limit) {
		if (!ruleId.equals("")) {
			return eventsToJson(eventService.getEventsForRule(ruleId, limit, prefetch));
		}
		if (!ruleClass.equals("")) {
			return eventsToJson(eventService.getEventsByRuleClass(ruleClass, limit, prefetch));
		}
		return eventsToJson(eventService.getLatestEvents(limit, prefetch));
	}

	@NotNull
	private String eventsToJson(List<ODocument> list) {
		if (list == null)
			return "[]";
		return list.stream()
				.map(d -> d.toJSON())
				.collect(Collectors.joining(",", "[", "]")).toString();
	}


}
