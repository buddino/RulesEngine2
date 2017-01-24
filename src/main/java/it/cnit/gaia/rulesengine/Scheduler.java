package it.cnit.gaia.rulesengine;

import io.swagger.client.ApiException;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.Fireable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class Scheduler {
    private final Logger LOGGER = Logger.getRootLogger();

    @Autowired
    MeasurementRepository measurements;
    @Autowired
	RulesLoader rulesLoader;

    @PostConstruct
	public void init(){
		LOGGER.info("Running Recommendation Engine");
		Fireable f = rulesLoader.getRuleTree("#25:1");
		measurements.getUriSet().forEach(s -> LOGGER.info(s));
	}

    //@Scheduled(fixedDelay = 60000)
    public void scheduledMethod() throws ApiException {
		LOGGER.info("Running Recommendation Engine");
		measurements.updateLatest();
		Fireable f = rulesLoader.getRuleTree("#25:1");
		f.fire();
    }
    //@Scheduled(fixedDelay = 1000)
    public void repeatingrule(){

	}

}
