package it.cnit.gaia.rulesengine.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.*;
import org.apache.log4j.Logger;

/**
 * Created by cuffaro on 29/11/2016.
 */
public class SwaggerTokenRequest {
	Logger LOGGER = Logger.getLogger(this.getClass().getSimpleName());
    private String access_token = null;
    private Long expires_in = null;

    public SwaggerTokenRequest(String username, String password, String secret, String clientname) {
        Gson J = new Gson();
        OkHttpClient client = new OkHttpClient();
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        RequestBody formBody = formEncodingBuilder
                .add("client_id", clientname)
                .add("client_secret", secret)
                .add("scope", "read")
                .add("grant_type", "password")
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url("https://sso.sparkworks.net/aa/oauth/token")
                .post(formBody)
                .build();
		String body = null;
        try {
            Response response = client.newCall(request).execute();
            body = response.body().string();
            JsonObject resp = J.fromJson(body, JsonObject.class);
            this.access_token = resp.get("access_token").getAsString();
            this.expires_in = resp.get("expires_in").getAsLong();
        }
        catch (Exception e){
			LOGGER.error(e.getMessage());
			LOGGER.error(body);
			System.exit(1);
		}
    }

    public String getAccess_token() {
        return access_token;
    }

    public Long getExpires_in() {
        return expires_in;
    }
}
