package it.cnit.gaia.rulesengine.configuration;

import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrientConfiguration {
	//TODO Externalize configuration
	@Bean
	public OrientGraph orientdb(){
		return new OrientGraph("remote:localhost/Prova","root","e4aa120");
	}
}
