package it.cnit.gaia.rulesengine.measurements;

import io.swagger.client.ApiException;
import io.swagger.client.model.ResourceDataDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MeasurementRepository  {

	Logger LOGGER = Logger.getLogger(this.getClass().getSimpleName());
	Set<String> uriSet = new HashSet<>();

    @Autowired
	@Qualifier("sparks")
	MeasurementService sparks;

    Date lastupdate = null;
    Map<String, ResourceDataDTO> latestReadings = new HashMap<String, ResourceDataDTO>();
    Map<String, List<ResourceDataDTO>> lastHourRedings = new HashMap<String, List<ResourceDataDTO>>();

    public Date getLastUpdate() {
        return lastupdate;
    }

    public void updateLatest() {
        LOGGER.info("Updating resources...");
        try {
            latestReadings = sparks.queryLatest();
            lastupdate = new Date();
        } catch (ApiException e) {
            e.printStackTrace();
        }
        LOGGER.info("Updated resources");
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

    public Map<String, ResourceDataDTO> getLatestReadings() {
        return Collections.unmodifiableMap(latestReadings);
    }

    public Map<String, List<ResourceDataDTO>> getLastHours() {
        return Collections.unmodifiableMap(lastHourRedings);
    }

    public ResourceDataDTO getLatestFor(String uri) {
        if (latestReadings.containsKey(uri))
            return latestReadings.get(uri);
        LOGGER.error(uri + " not found in map");
		LOGGER.info("Trying updating resources"); //FIXME
		updateLatest();
		return latestReadings.get(uri);
    }

    public List<ResourceDataDTO> getLatestHourFor(String uri) {
        if (lastHourRedings.containsKey(uri))
            return lastHourRedings.get(uri);
        LOGGER.error(uri + " not found in map");
        return null;
    }

    public boolean addUri(String uri){
        return uriSet.add(uri);
    }
    public Set<String> getUriSet(){ return uriSet; }

	public Map<String, Long> updateMeterMap() {
		int counter = 0;
		LOGGER.info(String.format("Resolving URIs (%d)", uriSet.size()));
		Map<String, Long> map = new HashMap<>();
		for (String uri : uriSet) {
			try {
				Long resourceId = sparks.uri2id(uri);
				map.put(uri, resourceId);
				counter++;
				LOGGER.debug(String.format("%s: %d", uri, resourceId));
				if (counter % 10 == 0) {
					LOGGER.info(String.format("%d/%d mapped", counter, uriSet.size()));
				}
			} catch (ApiException e) {
				LOGGER.error(String.format("[%s] -> %s", uri, e.getMessage()));
			}
		}
		LOGGER.info(String.format("Mapped %d URIs", map.entrySet().size()));
		sparks.setMeterMap(map);
		return map;
	}

	public Map<String, Long> getMeterMap() {
		return sparks.getMeterMap();
	}


    //TODO Add a method to start/update the mapping URI-->ResourceID
}
