package it.cnit.gaia.rulesengine.rules;

import com.weatherlibrary.datamodel.Hour;
import com.weatherlibrary.datamodel.Weather;
import io.swagger.client.ApiException;
import io.swagger.client.model.AnalyticsResourceDataResponseDTO;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TemperatureForecast extends GaiaRule {

	@URI
	@LoadMe(required = false)
	public String ext_temp_uri;

	@LoadMe(required = false)
	@LogMe
	public Double threshold = 8.0;

	@LogMe
	public Double temp_today;

	@LogMe
	public Double temp_forecast;


	//TODO Coordinates from school
	private Double lat = 43.87;
	private Double lon = 11.06;

	@Override
	public boolean condition() {
		String timezone;

		//Retrieve the temperature forecast

		if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.FRIDAY) {
			Weather weather = weatherService.getForecast(lat, lon, 3, 9);
			timezone = weather.getLocation().getTz_id();
			temp_forecast = weather.getForecast().getForecastday().get(2).getHour().get(0).getTemp_c();
		} else {
			Weather weather = weatherService.getForecast(lat, lon, 1, 9);
			timezone = weather.getLocation().getTz_id();
			temp_forecast = weather.getForecast().getForecastday().get(0).getHour().get(0).getTemp_c();
		}


		//Check if the temperature is too high for this
		if (temp_forecast > 15.0)
			return false;

		//Check if the rule is fired before 10am
		//Riguarda Is it useful?
		if (LocalDateTime.now().getHour() < 10) {
			LOGGER.debug(String.format("Rule [%s] not fired because it's too early (should be fired after 10am)", rid));
			return false;
		}

		//Retrieve the average external temperature of today between 8am and 10am
		temp_today = getTodayTemperature(timezone);

		//Check if null
		//Riguarda
		if (temp_today == null) {
			//TODO Backup to the temperature of today
		}


		return temp_today - temp_forecast > threshold;
	}


	private Long dayAtSpecificHour(LocalDateTime dateTime, int hour, ZoneId zoneId) {
		LocalDateTime athour = LocalDateTime
				.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth(), hour, 0, 0);
		ZonedDateTime zonedDateTime = athour.atZone(zoneId);
		return zonedDateTime.toInstant().toEpochMilli();
	}

	private Double getTodayTemperature(String timezone) {
		//If the external temperature sensor is available
		//TODO Timezone
		ZoneId timezoneOffset = ZoneId.of(timezone);
		if (ext_temp_uri != null) {
			Long ext_temp_id = measurements.getMeterMap().get(ext_temp_uri);
			Long eigtham = dayAtSpecificHour(LocalDateTime.now(), 7, timezoneOffset);
			Long tenam = dayAtSpecificHour(LocalDateTime.now(), 10, timezoneOffset);
			try {
				AnalyticsResourceDataResponseDTO result = measurements.getMeasurementService()
																	  .timeRangeQuery(ext_temp_id, eigtham, tenam);
				return result.getAverage();
			} catch (ApiException e) {
				LOGGER.warn(e.getMessage());
			}
		}
		//If not query an external service
		else {
			String today = LocalDateTime.now(timezoneOffset).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			Hour weatherAtNine = weatherService.getHistoryAtSpecificHour(lat,lon,today,9);
			return weatherAtNine.getTemp_c();
		}
		return null;
	}
}
