package it.cnit.gaia.rulesengine.rules;

import io.swagger.client.ApiException;
import io.swagger.client.model.SummaryDTO;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.model.event.GaiaEvent;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;

import java.util.OptionalDouble;

public class PowerFactor extends GaiaRule {

	@LogMe
	@LoadMe
	@URI
	public String pwf_uri;

	@LogMe
	@LoadMe(required = false)
	public Double pwf_threshold = 0.95;

	@LogMe
	@LoadMe(required = false)
	public int windowLength = 10;

	@LogMe
	public double average;


	@Override
	public boolean condition() {
		//If the resource ID is not present the rule is discarded, so you don't need null check
		try {
			SummaryDTO summary = measurements.getSummary(pwf_uri);
			OptionalDouble optionalAverage = summary.getDay().stream()
													.limit(windowLength)
													.filter(d -> d > 0.0)
													.mapToDouble(d -> d)
													.average();
			if (!optionalAverage.isPresent()) {
				LOGGER.warn(String.format("[%s] Cannot compute the average beacuse there are no valid values.", rid));
				return false;
			}

			average = optionalAverage.getAsDouble();
			if (average < pwf_threshold) {
				return true;
			}
		} catch (ApiException e) {
				LOGGER.error("Error: "+pwf_uri, e);
		}
		return false;
	}

	@Override
	public void action() {
		GAIANotification notification = getBaseNotification();
		GaiaEvent event = getBaseEvent();
		if (average < 0.7) {
			notification.setSuggestion(String
					.format("The power factor average value in the latest %d days is %.3f. This is a critical low value and the energy provider can force you to solve this issue.", windowLength, average));
		} else {
			notification.setSuggestion(String
					.format("The power factor average value in the latest %d days is %.3f. Energy provider can add fees to you bill.", windowLength, average));
		}
		websocket.pushNotification(notification);
		eventService.addEvent(event);
	}

}
