package it.cnit.gaia.rulesengine.configuration;

import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.buildingdb.BuildingDatabaseServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BuildingDBConfiguration {

	@Bean
	public BuildingDatabaseService buildingdb() {
		return new BuildingDatabaseServiceImpl();
	}

}
