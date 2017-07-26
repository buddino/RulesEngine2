package it.cnit.gaia.rulesengine.configuration;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration for the Orient Database service
 */
@Configuration
@PropertySource("file:orientdb.properties")
public class OrientConfiguration {

	@Value("${orientdb.database}")
	String dbname;
	@Value("${orientdb.user}")
	String user;
	@Value("${orientdb.password}")
	String password;
	@Value("${orientdb.url}")
	String url;
	@Value("${orientdb.port}")
	String port;

	@Bean
	public OrientGraphFactory rulesdb(){
		return new OrientGraphFactory("remote:" + url + ":"+port+"/" + dbname, user, password).setupPool(1, 10);
	}


}
