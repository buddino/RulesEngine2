package it.cnit.gaia.rulesengine.configuration;


import it.cnit.gaia.api.MetadataAPI;
import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Provider for the Building Database Service
 */
@Configuration
@PropertySource("file:application.properties")
public class BuildingDatabaseServiceProvider {

	@Deprecated
	@Bean
	public BuildingDatabaseService buildingdb() {
		return new BuildingDatabaseService();
	}

	@Bean
	public MetadataAPI getMetaDataService(){
		return new MetadataAPI();
	}

}
