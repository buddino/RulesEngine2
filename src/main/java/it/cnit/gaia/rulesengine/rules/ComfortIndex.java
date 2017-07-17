package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;

public class ComfortIndex extends GaiaRule {

	//Thresholds
	@LogMe
	@LoadMe(required = false)
	public Double threshold = 30.0;

	//URIs
	@LogMe
	@LoadMe
	@URI
	public String temperature_uri;
	@LogMe
	@LoadMe
	@URI
	public String humidity_uri;

	//Values
	@LogMe
	public Double index;
	@LogMe
	public Double temp;
	@LogMe
	public Double humid;

	@Override
	public boolean condition() {
		try {
			if (!(metadataService.isTeaching(school.aid) && metadataService.isOccupied(getParentAreaId())))
				return false;
		} catch (Exception e) {
			LOGGER.warn("Error while retrieving schedule/calendar: "+ e.getMessage());
		}

		//Source: http://www.azosensors.com/article.aspx?ArticleID=487
		temp = measurements.getLatestFor(temperature_uri).getReading();
		humid = measurements.getLatestFor(humidity_uri).getReading();
		index = -8.7847 + 1.6114 * temp + 2.3385 * humid
				- 0.1461 * humid * temp - 0.0123 * temp * temp - 0.0164 * humid * humid
				+ 2.2117 * Math.pow(10, -3) * temp * temp * humid
				+ 7.2546 * Math.pow(10, -4) * temp * humid * humid
				- 3.5820 * Math.pow(10, -6) * temp * temp * humid * humid;
		return index > threshold;
	}


}
