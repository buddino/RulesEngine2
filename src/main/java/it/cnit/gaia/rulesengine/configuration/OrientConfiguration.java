package it.cnit.gaia.rulesengine.configuration;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrientConfiguration {
	//TODO Externalize configuration
	@Bean
	public OrientGraphFactory orientdb(){
		return new OrientGraphFactory("remote:localhost/Prova","root","e4aa120").setupPool(1,10);
	}
}
