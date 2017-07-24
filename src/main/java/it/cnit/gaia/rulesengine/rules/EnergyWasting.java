package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.api.dto.EventDTO;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.utils.HardCodedValues;
import org.joda.time.DateTime;

import java.util.List;

public class EnergyWasting extends GaiaRule {
	@LogMe
	@LoadMe
	@URI
	public String power_uri;

	@LogMe
	@LoadMe(required = false)
	@URI
	public String occupancy_uri;

	@LogMe
	@LoadMe
	public Double standby_threshold;

	@LogMe
	@LoadMe
	public Double on_threshold;

	@LogMe
	public Double power_value;

	@LoadMe(required = false)
	public int interval = 1;
	@LoadMe(required = false)
	public int times = 3;


	@Override
	public boolean condition() {
		if (isOccupied())
			return false;
		//Area not occupied
		power_value = measurements.getLatestFor(power_uri).getReading();
		return power_value > standby_threshold;
	}

	@Override
	public void action() {
		if (power_value > on_threshold) {
			//Device ON threshold
			//Notify and Log the event, normal behaviour
			//suggestion = ""; //TODO
			super.action();
		} else {
			//Some devices may be have left in standby
			eventService.addEvent(getBaseEvent());
			DateTime now = DateTime.now();
			List<EventDTO> eventsForRule = eventService
					.getLatestEventsForRule(rid, now.minusHours(interval).getMillis(), now.getMillis());
			if (eventsForRule.size() > times) {
				//Send notification only after times
				//suggestion = "";
				websocket.pushNotification(getBaseNotification());
			}


		}
	}

	private boolean isOccupied() {
		if (occupancy_uri == null) {
			try {
				return !metadataService.isClosed(school.aid) && metadataService.isOccupied(area.aid);
			} catch (BuildingDatabaseException e) {
				LOGGER.warn(e.getMessage());
			}
		}
		return measurements.getLatestFor(occupancy_uri).getReading() > HardCodedValues.occupancyThreshold;
	}


}
