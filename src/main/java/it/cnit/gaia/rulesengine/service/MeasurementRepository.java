package it.cnit.gaia.rulesengine.service;


import io.swagger.sparks.ApiException;
import io.swagger.sparks.model.ResourceAnalyticsDataResponseAPIModel;
import io.swagger.sparks.model.ResourceQueryCriteriaRequestWithinATimeframeAPIModel;
import io.swagger.sparks.model.SingleResourceMeasurementAPIModel;
import io.swagger.sparks.model.TheResourceSummaryDataAPIModel;
import it.cnit.gaia.rulesengine.model.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
@PropertySource("file:application.properties")
public class MeasurementRepository {

	Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	Set<String> uriSet = new HashSet<>();
	Date latestUpdate = null;
	Map<String, SingleResourceMeasurementAPIModel> latestReadings = new HashMap<>();
	Map<String, List<SingleResourceMeasurementAPIModel>> lastHourRedings = new HashMap<>();
	@Value("${scheduler.iteration_size}")
	int iterationSize;

	@Autowired
	SparksService sparks;

	public Date getLastUpdate() {
		return latestUpdate;
	}

	public boolean updateLatest() {
		LOGGER.info("Updating resources...");
		try {
			//latestReadings = sparks.queryLatest();
			latestReadings = sparks.queryLatestIteration(iterationSize);
			if (latestReadings==null){
				LOGGER.debug("Error updating resources. Query returned null with ");
				return false;
			}
			latestUpdate = new Date();
		} catch (ApiException e) {
			if (e.getCode() == 401) {
				LOGGER.error("Unauthorized.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LOGGER.info("Updated resources");
		return true;
	}

	public void updateLatestHour() {
		LOGGER.info("Updating latest hours...");
		try {
			lastHourRedings = sparks.queryLatestHour();
		} catch (ApiException e) {
			e.printStackTrace();
		}
		LOGGER.info("Updated resources");
	}

	public Map<String, SingleResourceMeasurementAPIModel> getLatestReadings() {
		return Collections.unmodifiableMap(latestReadings);
	}

	public Map<String, List<SingleResourceMeasurementAPIModel>> getLastHours() {
		return Collections.unmodifiableMap(lastHourRedings);
	}

	public SingleResourceMeasurementAPIModel getLatestFor(String uri) {
		if (latestReadings.containsKey(uri))
			return latestReadings.get(uri);
		LOGGER.error(uri + " not found in map");
		LOGGER.info("Trying updating resources"); //FIXME
		updateLatest();
		if (latestReadings.get(uri) == null) {
			throw new ResourceNotFoundException("Resource URI: " + uri);
		}
		return latestReadings.get(uri);
	}

	public List<SingleResourceMeasurementAPIModel> getLatestHourFor(String uri) {
		if (lastHourRedings.containsKey(uri))
			return lastHourRedings.get(uri);
		LOGGER.error(uri + " not found in accountInformation");
		return null;
	}

	public boolean addUri(String uri) {
		return uriSet.add(uri);
	}

	public Set<String> getUriSet() {
		return uriSet;
	}

	public Map<String, Long> updateMeterMap() {
		LOGGER.debug(String.format("Resolving URIs (%d)", uriSet.size()));
		Map<String, Long> map = sparks.getMeterMap();
		uriSet.parallelStream().forEach(uri -> {
			//Check if the map already contains the mapping for the URI
			if (!map.containsKey(uri)) {
				try {
					//If not, query for the mapping
					Long resourceId = sparks.uri2id(uri);
					map.put(uri, resourceId);
					LOGGER.debug(String.format("%s: %d", uri, resourceId));
				} catch (ApiException e) {
					LOGGER.error(String.format("[%s] -> %s", uri, e.getMessage()));
				}
			}
		});
		return map;
	}

	public Long checkUri(String uri) throws ApiException {
		return sparks.uri2id(uri);
	}

	public SparksService getMeasurementService() {
		return sparks;
	}

	public TheResourceSummaryDataAPIModel getSummary(Long resourceId) throws ApiException {
		TheResourceSummaryDataAPIModel summary = sparks.getSummary(resourceId);
		return summary;
	}

	public TheResourceSummaryDataAPIModel getSummary(String resourceURI) throws ApiException {
		Long resourceId = getMeterMap().get(resourceURI);
		return getSummary(resourceId);
	}

	public Map<String, Long> getMeterMap() {
		return sparks.getMeterMap();
	}

	public ResourceAnalyticsDataResponseAPIModel getTimeRange(Long aid, Long from, Long to, ResourceQueryCriteriaRequestWithinATimeframeAPIModel.GranularityEnum granularity) throws ApiException {
		return sparks.timeRangeQuery(aid, from, to, granularity);
	}

	public ResourceAnalyticsDataResponseAPIModel getTimeRange(String uri, Long from, Long to, ResourceQueryCriteriaRequestWithinATimeframeAPIModel.GranularityEnum granularity) throws ApiException {
		Long aid = getMeterMap().get(uri);
		if (aid == null) {
			LOGGER.error(uri + " not found in map");
			return null;
		}
		return sparks.timeRangeQuery(aid, from, to, granularity);
	}

	public boolean isUriPresent(String uri) {
		return sparks.getMeterMap().get(uri) != null;
	}

	public Date getLatestUpdate() {
		return latestUpdate;
	}

	public Double getAverage(Long id, Long from, Long to) throws ApiException {
		ResourceAnalyticsDataResponseAPIModel result = sparks.timeRangeQuery(id, from, to, ResourceQueryCriteriaRequestWithinATimeframeAPIModel.GranularityEnum.HOUR);
		if (result == null)
			return null;
		return result.getAverage();
	}
}
