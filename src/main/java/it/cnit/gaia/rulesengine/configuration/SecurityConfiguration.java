package it.cnit.gaia.rulesengine.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

/**
 * Configuration for the AAA system
 * Pre-Authorization
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile("default")
@PropertySource("file:account.properties")
public class SecurityConfiguration extends ResourceServerConfigurerAdapter {

	@Value("${aaa.check_token}")
	private String check_token;
	@Value("${aaa.secret}")
	private String secret;
	@Value("${aaa.client}")
	private String clientname;

	/**
	 * RemoteTokenService for checking information about the user (ROLE etc.)
	 * @return
	 */
	@Primary
	@Bean
	public RemoteTokenServices tokenService() {
		//TODO Externalize configuraton
		RemoteTokenServices tokenService = new RemoteTokenServices();
		tokenService.setCheckTokenEndpointUrl(check_token);
		tokenService.setClientId(clientname);
		tokenService.setClientSecret(secret);
		return tokenService;
	}

	/**
	 * Allow preflight requests and OPTIONS requests
	 * Allow only authenticated users with role USER (to be modified according to permissions)
	 * Allow requests to the SockJS websocket endpoint
	 * Allow access to documentation ?
	 * @param http
	 * @throws Exception
	 */
	@Override
	public void configure(HttpSecurity http) throws Exception {
		//http.authorizeRequests().antMatchers("/**").permitAll();
		//http.authorizeRequests().requestMatchers(CorsUtils::isPreFlightRequest).permitAll().antMatchers("/**").hasRole("USER");
		//http.authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll().antMatchers("/**").hasRole("USER");

		http.cors()																									//Enable CORS
			.and().authorizeRequests().antMatchers("/docs/*").permitAll().and().authorizeRequests()		//Allows Swagger API
			.antMatchers("/v2/api-docs").permitAll()													//Allows Swagger API
			.and().authorizeRequests().antMatchers("/gs-guide-notification/**").permitAll()				//Allows web socket
			.and().authorizeRequests().antMatchers("/**").hasAnyRole(
					"GAIA_STUDENT","GAIA_TEACHER","GAIA_LOCAL_MANAGER","GAIA_GLOBAL_MANAGER","GAIA_ADMIN");				//Block all other dto except from user with ROLE.USER
	}

}
