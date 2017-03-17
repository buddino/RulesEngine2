package it.cnit.gaia.rulesengine.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

@Configuration
@EnableResourceServer
public class RestControllerAAA extends ResourceServerConfigurerAdapter {

	@Primary
	@Bean
	public RemoteTokenServices tokenService() {
		RemoteTokenServices tokenService = new RemoteTokenServices();
		tokenService.setCheckTokenEndpointUrl("https://sso.sparkworks.net/aa/oauth/check_token");
		tokenService.setClientId("gaia-prato");
		tokenService.setClientSecret("27d7ecb0-4563-4815-95c8-98f55899b852");
		return tokenService;
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				.antMatchers("/**")
				.access("#oauth2.hasScope('read')");
	}

}
