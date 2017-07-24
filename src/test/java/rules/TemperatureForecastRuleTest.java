package rules;


import com.weatherlibrary.datamodel.*;
import io.swagger.client.ApiException;
import io.swagger.client.model.ResourceAnalyticsDataResponseAPIModel;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.rules.TemperatureForecast;
import it.cnit.gaia.rulesengine.service.SparksService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TemperatureForecastRuleTest extends GenericRuleTest{

	TemperatureForecast rule;
	@Mock
	SparksService sparksService;
	@Mock
	ResourceAnalyticsDataResponseAPIModel externalTemperatureResult;


	@Before
	public void setUp() throws RuleInitializationException {
		MockitoAnnotations.initMocks(this);
		rule = new TemperatureForecast();
		setUpRule(rule);
		when(measurementRepository.getMeasurementService()).thenReturn(sparksService);
	}


	@Test
	public void testDecreaseButOverThreshold() throws ApiException {
		//Tomorrow
		setWeatherForecast(21.0, "Europe/Rome");
		//Today by sensor
		setCurrentTemperatureFromWebService(37.0);
		Assert.assertFalse(rule.condition());
	}

	@Test
	public void testWebserviceTriggerDecrease() throws ApiException {
		//Tomorrow
		setWeatherForecast(5.0, "Europe/Rome");
		//Today by sensor
		setCurrentTemperatureFromWebService(20.0);
		Assert.assertTrue(rule.condition());
	}

	@Test
	public void testWebserviceNotTriggerEqual() throws ApiException {
		//Tomorrow
		setWeatherForecast(5.0, "Europe/Rome");
		//Today by webservice
		setCurrentTemperatureFromWebService(5.0);
		Assert.assertFalse(rule.condition());
	}

	@Test
	public void testSensorTriggerDecrease() throws ApiException {
		//Not null sensor --> Use sensor
		rule.ext_temp_uri = "notnull";
		//Tomorrow
		setWeatherForecast(5.0, "Europe/Rome");
		//Today by webservice (difference > 15)
		setCurrentTemperatureFromSensor(20.0);
		Assert.assertTrue(rule.condition());
	}

	@Test
	public void testSensorNotTriggerDecrease(){
		//Not null sensor --> Use sensor
		rule.ext_temp_uri = "notnull";
		//Tomorrow
		setWeatherForecast(5.0, "Europe/Rome");
		//Today by webservice (difference > 15)
		setCurrentTemperatureFromSensor(7.0);
		Assert.assertFalse(rule.condition());
	}

	@Test
	public void testSensorNotTriggerIncrease(){
		//Not null sensor --> Use sensor
		rule.ext_temp_uri = "notnull";
		//Tomorrow
		setWeatherForecast(21.0, "Europe/Rome");
		//Today by webservice (difference > 15)
		setCurrentTemperatureFromSensor(5.0);
		Assert.assertFalse(rule.condition());
	}

	private void setWeatherForecast(Double temp, String timezone){
		Weather weather = mock(Weather.class);
		Forecast forecast = mock(Forecast.class);
		Location location = mock(Location.class);
		when(location.getTz_id()).thenReturn(timezone);
		when(weather.getLocation()).thenReturn(location);
		Forecastday forecastday = mock(Forecastday.class);
		ArrayList<Forecastday> forecastdays = new ArrayList<>();
		forecastdays.add(forecastday);
		when(forecast.getForecastday()).thenReturn(forecastdays);
		Hour hour = mock(Hour.class);
		when(hour.getTemp_c()).thenReturn(temp);
		ArrayList<Hour> hours = new ArrayList<>();
		hours.add(hour);
		when(forecastday.getHour()).thenReturn(hours);
		when(weather.getForecast()).thenReturn(forecast);
		when(weatherService.getForecast(anyDouble(),anyDouble(),anyInt(),anyInt())).thenReturn(weather);
	}

	private void setCurrentTemperatureFromSensor(Double temp){
		when(externalTemperatureResult.getAverage()).thenReturn(temp);
		try {
			when(sparksService.timeRangeQueryHourly(anyLong(),anyLong(),anyLong())).thenReturn(externalTemperatureResult);
		} catch (ApiException e) {
			e.printStackTrace();
		}
	}

	private void setCurrentTemperatureFromWebService(Double temp){
		Hour hour = mock(Hour.class);
		when(hour.getTemp_c()).thenReturn(temp);
		when(weatherService.getHistoryAtSpecificHour(anyDouble(),anyDouble(),anyString(),anyInt())).thenReturn(hour);
	}

}
