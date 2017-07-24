package it.cnit.gaia.rulesengine.rules;

import io.swagger.client.ApiException;
import io.swagger.client.model.TheResourceSummaryDataAPIModel;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.model.event.GaiaEvent;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;

import java.util.OptionalDouble;

public class PowerFactor extends GaiaRule {
	//TODO Hard coded suggestions

	@LogMe
	@LoadMe
	@URI
	public String pwf_uri;

	@LogMe
	@LoadMe(required = false)
	public Double pwf_threshold = 0.95;

	@LogMe
	@LoadMe(required = false)
	public Double pwf_lowerthreshold = 0.70;

	@LogMe
	@LoadMe(required = false)
	public int windowLength = 10;

	@LoadMe(required = false)
	public String suggestion_low = "The power factor average value in the latest ${windowLength} days is ${average_pwf}. This is a critical low value and the energy provider can force you to solve this issue.";

	@LoadMe(required = false)
	public String suggestion_base = "The power factor average value in the latest ${windowLength} days is ${average_pwf}. Energy provider can add fees to you bill.";

	@LogMe
	public double average_pwf;


	@Override
	public boolean init() throws RuleInitializationException {
		if(suggestion==null && !suggestion.equals("")){
			suggestion_base = suggestion;
		}
		if(windowLength>40)
			warn("Window length must be less or equal than 40. Set to 40.");
			windowLength = 40;
		return super.init();
	}

	@Override
	public boolean condition() {
		//If the resource ID is not present the rule is discarded, so you don't need null check
		try {
			TheResourceSummaryDataAPIModel summary = measurements.getSummary(pwf_uri);
			summary.getDay();
			OptionalDouble optionalAverage = summary.getDay().stream()
													.limit(windowLength)
													.filter(d -> d > 0.0)
													.mapToDouble(d -> d)
													.average();
			if (!optionalAverage.isPresent()) {
				LOGGER.warn(String.format("[%s] Cannot compute the average_pwf beacuse there are no valid values.", rid));
				return false;
			}

			average_pwf = optionalAverage.getAsDouble();
			if (average_pwf < pwf_threshold) {
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
		if (average_pwf < pwf_lowerthreshold) {
			notification.setSuggestion(suggestion_low);
		} else {
			notification.setSuggestion(suggestion_base);
		}
		websocket.pushNotification(notification);
		eventService.addEvent(event);
	}

}
