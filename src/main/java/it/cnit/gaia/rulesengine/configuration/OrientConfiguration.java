package it.cnit.gaia.rulesengine.configuration;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrientConfiguration {
	//TODO Externalize configuration
	@Bean
	public OrientGraphFactory rulesdb(){
		return new OrientGraphFactory("remote:localhost/Prova","root","e4aa120").setupPool(1,10);
	}

	@Bean
	public OPartitionedDatabasePool eventdb(){
		return new OPartitionedDatabasePool("remote:localhost/EventLog","root","e4aa120");
	}
}
