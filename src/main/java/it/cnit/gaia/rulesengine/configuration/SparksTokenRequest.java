package it.cnit.gaia.rulesengine.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

public class SparksTokenRequest {
	private final String sso = "https://sso.sparkworks.net/aa/oauth/token";
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	private String access_token = null;
	private String refresh_token = null;
	private Long expires_in = null;
	private String username;
	private String password;
	private String secret;
	private String clientname;


	public SparksTokenRequest(String username, String password, String secret, String clientname) {
		int retryCounter = 0;
		this.username = username;
		this.password = password;
		this.secret = secret;
		this.clientname = clientname;
		FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
		RequestBody requestBody = formEncodingBuilder.add("client_id", clientname)
											   .add("client_secret", secret)
											   .add("scope", "read")
											   .add("grant_type", "password")
											   .add("username", username)
											   .add("password", password)
											   .build();
		while(true) {
			try {
				requestAccessToken(requestBody);
				return;
			} catch (IOException e) {
				retryCounter++;
				if( retryCounter == 3) {
					LOGGER.error("Error while requesting access token: " + e.getMessage());
				}
			}
		}
	}
	public void refreshToken() {
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
		try {
			requestAccessToken(requestBody);
		} catch (IOException e) {
			LOGGER.error("Error while requesting access token: " + e.getMessage());
		}
	}
	private void requestAccessToken(RequestBody formBody) throws AuthenticationException, IOException {
		OkHttpClient client = new OkHttpClient();
		ObjectMapper mapper = new ObjectMapper();
		Request request = new Request.Builder()
				.url(sso)
				.post(formBody)
				.build();
		String responseBody = client.newCall(request).execute().body().string();
		JsonNode resp = mapper.readTree(responseBody);
		if( resp.hasNonNull("access_token")) {
			access_token = resp.findValue("access_token").asText();
			expires_in = resp.get("expires_in").asLong();
			refresh_token = resp.get("refresh_token").asText();
		}
		else
			throw new AuthenticationCredentialsNotFoundException("Authentication failed");
	}
	public String getAccess_token() {
		return access_token;
	}
	public String getRefresh_token() {
		return refresh_token;
	}
	public Long getExpires_in() {
		return expires_in;
	}
}
