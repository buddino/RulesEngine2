package it.cnit.gaia.rulesengine.configuration;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class for the swagger documentation provider
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

	private final String PACKAGE_NAME = "it.cnit.gaia.rulesengine.api";
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).ignoredParameterTypes(ODocument.class)
													  .apiInfo(apiInfo())
													  .securityContexts(Arrays.asList(securityContext()))
													  .securitySchemes(Arrays.asList(securitySchema()))
													  .select().apis(RequestHandlerSelectors
						.basePackage(PACKAGE_NAME))
													  .build();
	}

	/**
	 * Information settings
	 * @return
	 */
	private ApiInfo apiInfo() {
		ApiInfoBuilder infoBuilder = new ApiInfoBuilder();
		return infoBuilder.title("GAIA Recommendation Engine")
						  .description("Documentation of the API for accessing and managin the GAIA recommendation engine")
						  .version("2.0").title("Recommendation Engine API docs")
						  .build();
	}

	/**
	 * Security schema
	 * @return
	 */
	private OAuth securitySchema() {
		AuthorizationScope authorizationScope = new AuthorizationScope("read", "read");
		LoginEndpoint loginEndpoint = new LoginEndpoint("https://sso.sparkworks.net/aa/oauth/token");
		GrantType grantType = new ClientCredentialsGrant(loginEndpoint.getUrl());
		return new OAuth("oauth2", Arrays.asList(authorizationScope), Arrays.asList(grantType));
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder()
							  .securityReferences(defaultAuth())
							  .forPaths(PathSelectors.any())
							  .build();
	}

	private List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("read", "read");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Arrays.asList(new SecurityReference("oauth2", authorizationScopes));
	}
}