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
import io.swagger.client.api.ResourceDataAPIApi;
import io.swagger.client.api.ResourceAPIApi;
import io.swagger.client.model.*;
import it.cnit.gaia.rulesengine.configuration.SwaggerTokenRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service(value = "sparks")
public class SparksService implements MeasurementService {
	private final ResourceDataAPIApi dataApi = new ResourceDataAPIApi();
	private final ResourceAPIApi resApi = new ResourceAPIApi();
	private final Logger LOGGER = Logger.getLogger(this.getClass().getSimpleName());
	Gson gson = new Gson();

	private Map<String, Long> meterMap = new HashMap<>();

	public SparksService() {
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

	@Override
	public Map<String, ResourceDataDTO> queryLatest() {
		Map<String, ResourceDataDTO> readings = new HashMap<>();
        QueryLatestResourceDataDTO query = new QueryLatestResourceDataDTO();
		try {
			for (Long resourceId : meterMap.values()) {
				QueryResourceDataCriteriaDTO crtierium = new QueryResourceDataCriteriaDTO();
				crtierium.setResourceID(resourceId);
				query.addQueriesItem(crtierium);
			}
		} catch (NullPointerException e) {
			LOGGER.error("You have to load the tree to fill the URIs set before querying for measuremeents");
			e.printStackTrace();
		}
		if(query.getQueries().size()>0) {
			QueryLatestResourceDataResultDTO result = null;
			try {
				result = dataApi.queryLatestValuesForResources(query);
			} catch (ApiException e) {
				LOGGER.error("query: "+query);
				LOGGER.error(e.getMessage());
			}
			Set<String> keySet = result.getResults().keySet();
			for (String requestString : keySet) {
				JsonObject obj = gson.fromJson(requestString, JsonObject.class);
				String resourceURI = obj.get("resourceURI").toString().replace("\"", "");
				ResourceDataDTO res = result.getResults().get(requestString);
				readings.put(resourceURI, res);
			}
		}
		else
			LOGGER.warn("The resource list is empty, cannot update measurements");
		return readings;
    }

	@Override
	public Map<String, List<ResourceDataDTO>> queryLatestHour() throws ApiException {
		Map<String, List<ResourceDataDTO>> readings = new HashMap<>();

        DateTime now = new DateTime();
        QueryTimeRangeResourceDataDTO query = new QueryTimeRangeResourceDataDTO();
		for (Long resourceId : meterMap.values()) {
			QueryTimeRangeResourceDataCriteriaDTO crtierium = new QueryTimeRangeResourceDataCriteriaDTO();
            crtierium.setResourceID(resourceId);
            crtierium.setFrom(now.minusHours(1).getMillis());
            crtierium.setTo(now.getMillis());
            crtierium.setGranularity(QueryTimeRangeResourceDataCriteriaDTO.GranularityEnum._5MIN);
            query.addQueriesItem(crtierium);
        }

		QueryTimeRangeResourceDataResultDTO result = dataApi.queryLatestValuesForResourcesWithinTimeWindow(query);
        Set<String> keySet = result.getResults().keySet();
        for (String requestString : keySet) {
            JsonObject obj = gson.fromJson(requestString, JsonObject.class);
            String resourceURI = obj.get("resourceURI").toString().replace("\"", "");
            List measurements = (List) result.getResults().get(requestString);
            JsonArray arr = (JsonArray) gson.toJsonTree(measurements);
            List<ResourceDataDTO> list = gson.fromJson(arr, new TypeToken<List<ResourceDataDTO>>() {
            }.getType());
            readings.put(resourceURI, list);
        }
        return readings;
    }

	@Override
	public Long uri2id(String uri) throws ApiException {
		//Check if the URI is instead a resourceID
		if(StringUtils.isNumeric(uri)) {
			return Long.valueOf(uri);
		}
		//Query for the resource ID of the URI
		ResourceDTO res = resApi.retrieveResourceByUri(uri);
		return res.getResourceId();
	}

	@Override
	public MeasurementService setMeterMap(Map<String, Long> meterMap) {
		this.meterMap = meterMap;
		return this;
	}

	@Override
	public SummaryDTO getSummary(Long resourceId) throws ApiException {
		return dataApi.retrieveLatestSummary(resourceId);
	}

	@Override
	public Map<String, Long> getMeterMap() {
		return meterMap;
	}
}
