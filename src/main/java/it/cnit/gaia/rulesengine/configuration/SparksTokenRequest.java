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


	public SparksTokenRequest(String username, String password, String secret, String clientname)  {
		int retryCounter = 0;
		this.username = username;
		this.password = password;
		this.secret = secret;
		this.clientname = clientname;

	}

	public String requestAccessToken() throws IOException {
		FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
		RequestBody requestBody = formEncodingBuilder.add("client_id", clientname)
													 .add("client_secret", secret)
													 .add("scope", "read")
													 .add("grant_type", "password")
													 .add("username", username)
													 .add("password", password)
													 .build();
		requestAccessToken(requestBody);
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
		requestAccessToken(requestBody);

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
		if (resp.hasNonNull("access_token")) {
			access_token = resp.findValue("access_token").asText();
			expires_in = resp.get("expires_in").asLong();
			refresh_token = resp.get("refresh_token").asText();
		} else
			throw new AuthenticationCredentialsNotFoundException("Latest token: " + access_token);
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
}