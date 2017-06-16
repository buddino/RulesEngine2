package it.cnit.gaia.rulesengine.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends ResourceServerConfigurerAdapter {

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
		//http.authorizeRequests().antMatchers("/**").permitAll();
		//http.authorizeRequests().requestMatchers(CorsUtils::isPreFlightRequest).permitAll().antMatchers("/**").hasRole("USER");
		//http.authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll().antMatchers("/**").hasRole("USER");

		http.cors()																									//Enable CORS
			.and().authorizeRequests().antMatchers("/docs/*").permitAll().and().authorizeRequests()		//Allows Swagger API
			.antMatchers("/v2/api-docs").permitAll()													//Allows Swagger API
			.and().authorizeRequests().antMatchers("/gs-guide-notification/**").permitAll();				//Allows web socket //Riguarda
			//.and().authorizeRequests().antMatchers("/**").hasRole("USER");							//Block all other dto except from user with ROLE.USER
	}

}
