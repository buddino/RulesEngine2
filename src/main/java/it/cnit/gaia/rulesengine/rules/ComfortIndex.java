package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.annotation.FromConfiguration;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.ToBeLogged;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import it.cnit.gaia.rulesengine.model.notification.NotificationType;

public class ComfortIndex extends GaiaRule {

	@ToBeLogged
	@FromConfiguration
	public Double threshold = 10.0;
	@ToBeLogged
	@FromConfiguration
	public String temperature_uri;
	@ToBeLogged
	@FromConfiguration
	public String humidity_uri;
	@ToBeLogged
	public Double index;
	@ToBeLogged
	public Double temp;
	@ToBeLogged
	public Double humid;

	@Override
	public boolean condition() {
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

	@Override
	public void action() {
		GAIANotification notification = getBaseNotification().setType(NotificationType.error);
	}

	public Double getThreshold() {
		return threshold;
	}

	public ComfortIndex setThreshold(Double threshold) {
		this.threshold = threshold;
		return this;
	}

	public String getTempUri() {
		return temperature_uri;
	}

	public ComfortIndex setTempUri(String tempUri) {
		this.temperature_uri = tempUri;
		return this;
	}

	public String getHumidUri() {
		return humidity_uri;
	}

	public ComfortIndex setHumidUri(String humidUri) {
		this.humidity_uri = humidUri;
		return this;
	}


}
