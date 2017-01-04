package it.cnit.gaia.rulesengine.measurements;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MeterMap {

    private final Logger LOGGER = Logger.getLogger(this.getClass());

    @Autowired
    SwaggerClient sparks;

    Map<String, Long> metermap = new HashMap<>();
    boolean isConfigured = false;

    @PostConstruct
    public void init() {
        LOGGER.info("Started resource mapping");
        InputStream in = null;
        try {
            in = new FileInputStream("./meters.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            //TODO Use the new /uri endpoint for retrieving the resourceIDMeterMap
            while ((line = br.readLine()) != null) {
                String[] splittedLine = line.split(",");
                String resourceUri = splittedLine[0];
                long resourceId = Long.parseLong(splittedLine[1]);
                /*
                //Maps uri -> id at every launch
                ResourceListDTO response = sparks.resApi.listUsingGET1(uri, "", false, "");
                if (response.getResources().size() > 0) {
                    Long resourceId = response.getResources().get(0).getResourceId();
                    metermap.put(uri, resourceId);
                    LOGGER.info("Created mapping " + uri + " : " + resourceId);
                } else {
                    System.err.println("Resource " + uri + " not found");
                }
                */
                metermap.put(resourceUri, resourceId);
            }
            isConfigured = true;
            LOGGER.info(String.format("Mapped %d resources", metermap.keySet().size()));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public Long getId(String uri) {
        return metermap.get(uri);
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    public List<Long> getIDs() {
        return new ArrayList(metermap.values());
    }
}
