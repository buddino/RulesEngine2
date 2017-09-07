package it.cnit.gaia.rulesengine.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.swagger.sparks.ApiException;
import io.swagger.sparks.api.ResourceAPIApi;
import io.swagger.sparks.api.ResourceDataAPIApi;
import io.swagger.sparks.api.SiteAPIApi;
import io.swagger.sparks.model.*;
import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Service
@PropertySource("file:application.properties")
public class SparksService implements SparksAAAService{
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	//FIXME User only A Json lib (Jackson)
	private Gson gson = new Gson();
	private ResourceDataAPIApi dataApi = new ResourceDataAPIApi();
	private ResourceAPIApi resApi = new ResourceAPIApi();
	private SiteAPIApi siteApi = new SiteAPIApi();
	private Map<String, Long> meterMap = new ConcurrentHashMap<>();

	@Autowired
	SparksTokenRequest tokenRequest;

	@PostConstruct
	public void init(){
		tokenRequest.registerService(this);
	}

	public void forceTokenRefresh(){
		tokenRequest.renewAccessToken();
	}

	public void requestAccessToken(){
		tokenRequest.renewAccessToken();
	}

	private Map<String, SingleResourceMeasurementAPIModel> queryLatest(Collection<Long> ids) throws ApiException {
		Map<String, SingleResourceMeasurementAPIModel> readings = new HashMap<>();
		CollectionOfDataQueryCriteriaRequestAPIModel query = new CollectionOfDataQueryCriteriaRequestAPIModel();
		try {
			for (Long resourceId : ids) {
				ResourceQueryCriteriaRequestAPIModel crtierium = new ResourceQueryCriteriaRequestAPIModel();
				crtierium.setResourceID(resourceId);
				query.addQueriesItem(crtierium);
			}
		} catch (NullPointerException e) {
			LOGGER.error("You have to load the tree to fill the URI-set before querying for measuremeents");
			e.printStackTrace();
		}
		if (query.getQueries().size() > 0) {
			ResourceLatestDataResponseAPIModel result = null;
			try {
				result = dataApi.queryLatestValuesForResources(query);
			} catch (ApiException e) {
				if (e.getCode() == 401 || e.getCode() == 403) {
					tokenRequest.renewAccessToken();
					result = dataApi.queryLatestValuesForResources(query);
				}
			}
			if(result == null || result.getResults() == null){
				LOGGER.error("No results found while querying latest measurements. No measurement has been updated. :(");
				return null;
			}
			Set<String> keySet = result.getResults().keySet();
			for (String requestString : keySet) {
				JsonObject obj = gson.fromJson(requestString, JsonObject.class);
				String resourceURI = obj.get("resourceURI").toString().replace("\"", "");
				String resourceID = obj.get("resourceID").toString().replace("\"", "");
				SingleResourceMeasurementAPIModel res = result.getResults().get(requestString);
				if (meterMap.containsKey(resourceID))
					readings.put(resourceID, res);
				if (meterMap.containsKey(resourceURI))
					readings.put(resourceURI, res);
			}
		} else
			LOGGER.warn("The resource list is empty, cannot update measurementRepositorys");
		return readings;
	}

	public Map<String, SingleResourceMeasurementAPIModel> queryLatest() throws ApiException {
		Map<String, SingleResourceMeasurementAPIModel> readings = new HashMap<>();
		CollectionOfDataQueryCriteriaRequestAPIModel query = new CollectionOfDataQueryCriteriaRequestAPIModel();
		try {
			for (Long resourceId : meterMap.values()) {
				ResourceQueryCriteriaRequestAPIModel crtierium = new ResourceQueryCriteriaRequestAPIModel();
				crtierium.setResourceID(resourceId);
				query.addQueriesItem(crtierium);
			}
		} catch (NullPointerException e) {
			LOGGER.error("You have to load the tree to fill the URI-set before querying for measuremeents");
			e.printStackTrace();
		}
		if (query.getQueries().size() > 0) {
			ResourceLatestDataResponseAPIModel result = null;
			try {
				result = dataApi.queryLatestValuesForResources(query);
			} catch (ApiException e) {
				if (e.getCode() == 401 || e.getCode() == 403) {
					tokenRequest.renewAccessToken();
					result = dataApi.queryLatestValuesForResources(query);
				}
			}
			if(result == null || result.getResults() == null){
				LOGGER.error("No results found while querying latest measurements. No measurement has been updated. :(");
				return null;
			}
			Set<String> keySet = result.getResults().keySet();
			for (String requestString : keySet) {
				JsonObject obj = gson.fromJson(requestString, JsonObject.class);
				String resourceURI = obj.get("resourceURI").toString().replace("\"", "");
				String resourceID = obj.get("resourceID").toString().replace("\"", "");
				SingleResourceMeasurementAPIModel res = result.getResults().get(requestString);
				if (meterMap.containsKey(resourceID))
					readings.put(resourceID, res);
				if (meterMap.containsKey(resourceURI))
					readings.put(resourceURI, res);
			}
		} else
			LOGGER.warn("The resource list is empty, cannot update measurementRepositorys");
		return readings;
	}

	public Map<String, SingleResourceMeasurementAPIModel> queryLatestIteration(int size) throws ApiException {
		Collection<Long> ids = meterMap.values();
		Map<String, SingleResourceMeasurementAPIModel> result = new HashMap<>();
		Iterator<Long> iterator = ids.iterator();
		int counter = 0;
		while (iterator.hasNext()){
			List<Long> tempIds = new ArrayList<>();
			while (iterator.hasNext() && counter < size){
				Long next = iterator.next();
				tempIds.add(next);
				counter++;
			}
			counter = 0;
			LOGGER.debug("Query:");
			LOGGER.debug(tempIds.toString());
			Map<String, SingleResourceMeasurementAPIModel> tempResult = queryLatest(tempIds);
			tempIds.clear();
			if(tempResult==null){
				return null;
			}
			LOGGER.debug("Resource update iteration done.");
			result.putAll(tempResult);
		}
		return result;
	}

	public ResourceAnalyticsDataResponseAPIModel timeRangeQueryHourly(Long id, Long from, Long to) throws ApiException {
		ListOfQueryCriteriaWithinATimeframeRequestAPIModel query = new ListOfQueryCriteriaWithinATimeframeRequestAPIModel();
		ResourceQueryCriteriaRequestWithinATimeframeAPIModel criterium  = new ResourceQueryCriteriaRequestWithinATimeframeAPIModel();
		criterium.setFrom(from);
		criterium.setTo(to);
		criterium.setResourceID(id);
		criterium.setGranularity(ResourceQueryCriteriaRequestWithinATimeframeAPIModel.GranularityEnum.HOUR);
		query.addQueriesItem(criterium);
		ResourceQueryCriteriaWithinATimeframeResponseAPIModel result = dataApi
				.queryLatestValuesForResourcesWithinTimeWindow(query);
		if(result.getResults().values().size()==1) {
			return result.getResults().values().iterator().next();
		}
		return null;
	}

	public ResourceAnalyticsDataResponseAPIModel timeRangeQuery(Long id, Long from, Long to, ResourceQueryCriteriaRequestWithinATimeframeAPIModel.GranularityEnum granularity) throws ApiException {
		ListOfQueryCriteriaWithinATimeframeRequestAPIModel query = new ListOfQueryCriteriaWithinATimeframeRequestAPIModel();
		ResourceQueryCriteriaRequestWithinATimeframeAPIModel criterium  = new ResourceQueryCriteriaRequestWithinATimeframeAPIModel();
			criterium.setFrom(from);
			criterium.setTo(to);
			criterium.setResourceID(id);
			criterium.setGranularity(granularity);
		query.addQueriesItem(criterium);
		ResourceQueryCriteriaWithinATimeframeResponseAPIModel result = dataApi
				.queryLatestValuesForResourcesWithinTimeWindow(query);
		if(result.getResults().values().size()==1) {
			return result.getResults().values().iterator().next();
		}
		return null;
	}

	public Map<String, List<SingleResourceMeasurementAPIModel>> queryLatestHour() throws ApiException {
		Map<String, List<SingleResourceMeasurementAPIModel>> readings = new HashMap<>();

		DateTime now = new DateTime();
		ListOfQueryCriteriaWithinATimeframeRequestAPIModel query = new ListOfQueryCriteriaWithinATimeframeRequestAPIModel();

		for (Long resourceId : meterMap.values()) {
			ResourceQueryCriteriaRequestWithinATimeframeAPIModel crtierium = new ResourceQueryCriteriaRequestWithinATimeframeAPIModel();
			crtierium.setResourceID(resourceId);
			crtierium.setFrom(now.minusHours(1).getMillis());
			crtierium.setTo(now.getMillis());
			crtierium.setGranularity(ResourceQueryCriteriaRequestWithinATimeframeAPIModel.GranularityEnum._5MIN);
			query.addQueriesItem(crtierium);
		}

		ResourceQueryCriteriaWithinATimeframeResponseAPIModel result = dataApi
				.queryLatestValuesForResourcesWithinTimeWindow(query);
		Set<String> keySet = result.getResults().keySet();
		for (String requestString : keySet) {
			JsonObject obj = gson.fromJson(requestString, JsonObject.class);
			String resourceURI = obj.get("resourceURI").toString().replace("\"", "");
			List measurements = (List) result.getResults().get(requestString);
			JsonArray arr = (JsonArray) gson.toJsonTree(measurements);
			List<SingleResourceMeasurementAPIModel> list = gson.fromJson(arr, new TypeToken<List<SingleResourceMeasurementAPIModel>>() {
			}.getType());
			readings.put(resourceURI, list);
		}
		return readings;
	}

	public Long uri2id(String uri) throws ApiException {
		//Check if the URI is instead a resourceID
		if (StringUtils.isNumeric(uri)) {
			return Long.valueOf(uri);
		}
		//Query for the resource ID of the URI
		ResourceAPIModel res = resApi.retrieveResourceByUri(uri);
		return res.getResourceId();
	}

	public TheResourceSummaryDataAPIModel getSummary(Long resourceId) throws ApiException {
		return dataApi.retrieveLatestSummary1(resourceId);
	}

	public Map<String, Long> getMeterMap() {
		return meterMap;
	}

	public SparksService setMeterMap(Map<String, Long> meterMap) {
		this.meterMap = meterMap;
		return this;
	}

	public void checkAuth() throws IOException {
		try {
			dataApi.retrieveLatestValues(0L);
		} catch (ApiException e) {
			if(e.getCode()==401 || e.getCode()==403){
				LOGGER.warn("Not authorized, requesting access token");
				tokenRequest.renewAccessToken();
			}
		}
	}

	public ResourceDataAPIApi getDataApi() {
		return dataApi;
	}

	public ResourceAPIApi getResApi() {
		return resApi;
	}

	public SiteAPIApi getSiteApi() {
		return siteApi;
	}
	public List<Long> getSubsitesIds(Long id) throws ApiException {
		List<SiteAPIModel> sites = siteApi.retrieveSubSites(id).getSites();
		return sites.stream().map(SiteAPIModel::getId).collect(Collectors.toList());
	}
	public List<SiteAPIModel> getSubsites(Long id) throws ApiException {
		return siteApi.retrieveSubSites(id).getSites();
	}
	public SiteAPIModel getSite(Long id) throws ApiException {
		return siteApi.retrieveSite(id);
	}

	@Override
	public void setToken(String token) {
		dataApi.getApiClient().setAccessToken(token);
		resApi.getApiClient().setAccessToken(token);
		siteApi.getApiClient().setAccessToken(token);
	}
}
