package it.cnit.gaia.rulesengine.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import it.cnit.gaia.rulesengine.service.SparksAAAService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Configuration
@PropertySource("file:account.properties")
public class SparksTokenRequest {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	private String access_token = null;
	private String refresh_token = null;
	private Long expires_in = null;

	@Value("${aaa.sso}")
	private String sso;
	@Value("${aaa.username}")
	private String username;
	@Value("${aaa.password}")
	private String password;
	@Value("${aaa.secret}")
	private String secret;
	@Value("${aaa.client}")
	private String clientname;

	private List<SparksAAAService> tokenServices = new LinkedList<>();

	public void injectTokenIntoServices(){
		for(SparksAAAService service : tokenServices){
			service.setToken(access_token);
		}
	}

	public void renewAccessToken(){
		getNewAccessToken();
		injectTokenIntoServices();
	}

	public String getNewAccessToken() {
		FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
		RequestBody requestBody = formEncodingBuilder.add("client_id", clientname)
													 .add("client_secret", secret)
													 .add("scope", "read")
													 .add("grant_type", "password")
													 .add("username", username)
													 .add("password", password)
													 .build();
		requestAccessTokenFromRequestBody(requestBody);
		return access_token;
	}

	/**
	 * Use the refresh token to resfresh the access token
	 * @throws IOException
	 */
	public void refreshToken() throws IOException {
		LOGGER.info("Refreshing access token");
		FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
		RequestBody requestBody = formEncodingBuilder.add("client_id", clientname)
													 .add("client_secret", secret)
													 .add("scope", "read")
													 .add("grant_type", "refresh_token")
													 .add("refresh_token", refresh_token)
													 .add("username", username)
													 .add("password", password)
													 .build();
		requestAccessTokenFromRequestBody(requestBody);

	}

	private void requestAccessTokenFromRequestBody(RequestBody formBody) throws AuthenticationException {
		OkHttpClient client = new OkHttpClient();
		ObjectMapper mapper = new ObjectMapper();
		Request request = new Request.Builder()
				.url(sso)
				.post(formBody)
				.build();
		try {
			String responseBody = client.newCall(request).execute().body().string();
			JsonNode resp = mapper.readTree(responseBody);
			if (resp.hasNonNull("access_token")) {
				access_token = resp.findValue("access_token").asText();
				expires_in = resp.get("expires_in").asLong();
				refresh_token = resp.get("refresh_token").asText();
			} else
				throw new AuthenticationCredentialsNotFoundException("Latest token: " + access_token);
		}
		catch (IOException e){
			LOGGER.error("Error while requesting access token",e);
		}
	}

	public String getAccessToken() {
		return access_token;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public Long getExpires_in() {
		return expires_in;
	}

	public void registerService(SparksAAAService service) {
		tokenServices.add(service);
	}
}
