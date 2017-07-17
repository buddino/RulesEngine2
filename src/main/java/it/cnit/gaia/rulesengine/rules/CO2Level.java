package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;

public class CO2Level extends GaiaRule {
	@LogMe
	@LoadMe
	@URI
	public String co2_uri;

	@URI
	@LoadMe(required = false)
	public String in_temp_uri;

	@URI
	@LoadMe(required = false)
	public String out_temp_uri;

	@LogMe
	@LoadMe(required = false)
	public Double co2_threshold = 1000.0;

	@LogMe
	@LoadMe(required = false)
	public Double temp_diff_thresh = 5.0;

	@LogMe
	public Double co2_value;
	@LogMe
	public Double temp_diff;

	@LoadMe(required = false)
	String suggestion_base = "High CO2 level! Enhance the airflow in the room!";

	@LoadMe(required = false)
	String suggestion_over = "High CO2 level! Open the door to let some clean air in!";

	@LoadMe(required = false)
	String suggestion_below = "High CO2 level! Open the window to let some clean air in!";

	@Override
	public boolean condition() {
		if (in_temp_uri != null && out_temp_uri != null) {
			double in_temp = measurements.getLatestFor(in_temp_uri).getReading();
			double out_temp = measurements.getLatestFor(out_temp_uri).getReading();
			temp_diff = Math.abs(in_temp - out_temp);
		}
		co2_value = measurements.getLatestFor(co2_uri).getReading();
		if (co2_value > co2_threshold) {
			if (temp_diff == null) {
				setSuggestion(suggestion_base);
			} else if (temp_diff > temp_diff_thresh) {
				setSuggestion(suggestion_over);
			} else {
				setSuggestion(suggestion_below);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean init() throws RuleInitializationException {
		if(suggestion!=null && suggestion.equals("")){
			suggestion_base = suggestion;
		}
		return super.init();
	}
}
