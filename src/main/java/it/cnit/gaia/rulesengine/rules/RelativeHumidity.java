package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;

public class RelativeHumidity extends GaiaRule {
	@LogMe
	@LoadMe
	@URI
	public String in_humid_uri;
	/*
	@LogMe
	@LoadMe(required = false)
	@URI
	public String out_huimid_uri;

	@LogMe
	@LoadMe
	@URI
	public String in_temp_uri;

	@LogMe
	@LoadMe
	@URI
	public String out_temp_uri;
	*/

	@LogMe(event = false)
	@LoadMe(required = false)
	public Double humid_low_thresh = 30.0;

	@LogMe(event = false)
	@LoadMe(required = false)
	public Double humid_high_thresh = 80.0;

	@LogMe
	public Double humid_value;

	/*
	@LogMe
	@LoadMe(required = false)
	public Double temp_diff_thresh = 5.0;

	@LogMe
	public Double temp_diff*/

	@LoadMe(required = false)
	public String suggestion_low = "Low relative humidity! Try putting a plant inside the area.";

	@LoadMe(required = false)
	public String suggestion_high = "High relative humidity! Try installing a dehumidifier.";

	@Override
	public boolean condition() {
		humid_value = measurements.getLatestFor(in_humid_uri).getReading();
		if (humid_value > humid_high_thresh) {
			setSuggestion(suggestion_high);
			return true;
		} else if (humid_value < humid_low_thresh) {
			setSuggestion(suggestion_low);
			return true;
		}
		return false;
	}
}
