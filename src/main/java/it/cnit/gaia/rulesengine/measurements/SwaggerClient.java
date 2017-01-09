package it.cnit.gaia.rulesengine.measurements;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DataapicontrollerApi;
import io.swagger.client.api.ResourceapicontrollerApi;
import io.swagger.client.model.*;
import it.cnit.gaia.rulesengine.configuration.SwaggerTokenRequest;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class SwaggerClient {
    public final DataapicontrollerApi dataApi = new DataapicontrollerApi();
    public final ResourceapicontrollerApi resApi = new ResourceapicontrollerApi();
    Gson gson = new Gson();

    @Autowired
    MeterMap meterMap;

    public SwaggerClient() {
        //Read credentials from file
        Map<String,String> map = null;
        ApiClient client;
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

        //Request access token
        SwaggerTokenRequest tokenRequest = new SwaggerTokenRequest(map.get("username"),map.get("password"),map.get("secret"),map.get("client"));
        client = new ApiClient();
        client.setAccessToken(tokenRequest.getAccess_token());

        OkHttpClient httpClient = client.getHttpClient();
        //Set larger timeouts
        httpClient.setConnectTimeout(60, TimeUnit.SECONDS );
        httpClient.setReadTimeout(60, TimeUnit.SECONDS);
        httpClient.setWriteTimeout(60, TimeUnit.SECONDS);
        dataApi.setApiClient(client);
        resApi.setApiClient(client);
    }

    public Map<String, ResourceDataDTO> queryLatest() throws ApiException {
        Map<String, ResourceDataDTO> readings = new HashMap<>();
        QueryLatestResourceDataDTO query = new QueryLatestResourceDataDTO();
        for (Long resourceId : meterMap.getIDs()) {
            QueryResourceDataCriteriaDTO crtierium = new QueryResourceDataCriteriaDTO();
            crtierium.setResourceID(resourceId);
            query.addQueriesItem(crtierium);
        }
        QueryLatestResourceDataResultDTO result = dataApi.queryLatestResourcesDataUsingPOST(query);
        Set<String> keySet = result.getResults().keySet();
        for (String requestString : keySet) {
            JsonObject obj = gson.fromJson(requestString, JsonObject.class);
            String resourceURI = obj.get("resourceURI").toString().replace("\"", "");
            ResourceDataDTO res = result.getResults().get(requestString);
            readings.put(resourceURI, res);
        }
        return readings;
    }


    public Map<String, List<ResourceDataDTO>> queryLatestHour() throws ApiException {
        Map<String, List<ResourceDataDTO>> readings = new HashMap<>();

        DateTime now = new DateTime();
        QueryTimeRangeResourceDataDTO query = new QueryTimeRangeResourceDataDTO();
        for (Long resourceId : meterMap.getIDs()) {
            QueryTimeRangeResourceDataCriteriaDTO crtierium = new QueryTimeRangeResourceDataCriteriaDTO();
            crtierium.setResourceID(resourceId);
            crtierium.setFrom(now.minusHours(1).getMillis());
            crtierium.setTo(now.getMillis());
            crtierium.setGranularity(QueryTimeRangeResourceDataCriteriaDTO.GranularityEnum._5MIN);
            query.addQueriesItem(crtierium);
        }

		QueryTimeRangeResourceDataResultDTO result = dataApi.queryTimeRangeResourcesDataUsingPOST(query);
        Set<String> keySet = result.getResults().keySet();
        for (String requestString : keySet) {
            JsonObject obj = gson.fromJson(requestString, JsonObject.class);
            String resourceURI = obj.get("resourceURI").toString().replace("\"", "");
            List measurements = result.getResults().get(requestString);
            JsonArray arr = (JsonArray) gson.toJsonTree(measurements);
            List<ResourceDataDTO> list = gson.fromJson(arr, new TypeToken<List<ResourceDataDTO>>() {
            }.getType());
            readings.put(resourceURI, list);
        }
        return readings;
    }


}
