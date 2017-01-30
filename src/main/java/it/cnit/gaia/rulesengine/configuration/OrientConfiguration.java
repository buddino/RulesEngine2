package it.cnit.gaia.rulesengine.configuration;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

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

	@Bean
	public OrientGraphFactory rulesdb(){
		return new OrientGraphFactory("remote:" + url + "/" + dbname, user, password).setupPool(1, 10);
	}


}
