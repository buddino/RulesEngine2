package it.cnit.gaia.rulesengine.api;

import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import it.cnit.gaia.rulesengine.event.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

	@Autowired
	private EventService eventService;


	@RequestMapping("/events")
	public String getEvents() {
		return toJson(eventService.getLatestEvents(10, true));
	}

	private String toJson(Iterable<OrientVertex> res) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(res.iterator().next().getRecord().toJSON());
		for (OrientVertex v : res) {
			sb.append("," + v.getRecord().toJSON());
		}
		sb.append("]");
		return sb.toString();
	}
}
