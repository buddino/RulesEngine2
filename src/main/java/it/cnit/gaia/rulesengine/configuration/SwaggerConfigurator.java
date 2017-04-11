package it.cnit.gaia.rulesengine.configuration;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import io.swagger.client.ApiClient;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

public class SwaggerConfigurator {

    static private Map<String,String> map;
    static ApiClient apiClient;

    static {
        String confFile = "./account.yaml";
        FileReader fr = null;
        try {
            fr = new FileReader(confFile);
        } catch (FileNotFoundException e) {
            System.err.println("Account configuration file not found ['account.yaml']");
            e.printStackTrace();
        }
        YamlReader yaml = new YamlReader(fr);
        try {
            map = (Map<String, String>) yaml.read();
        } catch (YamlException e) {
            e.printStackTrace();
        }
        SparksTokenRequest tokenRequest = new SparksTokenRequest(map.get("username"),map.get("password"),map.get("secret"),map.get("client"));
        apiClient = new ApiClient();
        apiClient.setAccessToken(tokenRequest.getAccess_token());
    }

    private SwaggerConfigurator(){

    }

    public static ApiClient getApiClient(){
        return apiClient;
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
