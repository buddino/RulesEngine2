package it.cnit.gaia.rulesengine.configuration;

import com.weatherlibrary.WeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Provider for the Weather Service
 */
@Configuration
@PropertySource("file:application.properties")
public class WeatherServiceProvider {
	@Value("${weatherservice.apikey}")
	String appid;

	@Bean
	public WeatherService weatherService() {
		return new WeatherService(appid);
	}
}
