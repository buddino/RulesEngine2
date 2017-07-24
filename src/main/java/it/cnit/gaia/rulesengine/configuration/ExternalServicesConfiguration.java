package it.cnit.gaia.rulesengine.configuration;

import com.weatherlibrary.WeatherService;
import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:application.properties")

public class ExternalServicesConfiguration {

	@Value("${weatherservice.apikey}")
	String appid;

	@Bean
	public BuildingDatabaseService buildingdb() {
		return new BuildingDatabaseService();
	}

	@Bean
	public WeatherService weatherService(){
		return new WeatherService(appid);
	}


}
