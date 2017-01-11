package it.cnit.gaia.rulesengine.measurements;

import io.swagger.client.ApiException;
import io.swagger.client.model.ResourceDataDTO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MeasurementRepository  {
    //TODO update before giving date in lastUpdate is null
    Logger LOGGER = Logger.getLogger(this.getClass());

    Set<String> uriSet = new HashSet<>();

    @Autowired
    SwaggerClient sparks;

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
        return null;
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
}
