package it.cnit.gaia.rulesengine;

import io.swagger.client.ApiException;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.Fireable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class Scheduler {
	private final Logger LOGGER = Logger.getLogger(Scheduler.class.getSimpleName());


    @Autowired
    MeasurementRepository measurements;
    @Autowired
	RulesLoader rulesLoader;

    @PostConstruct
	public void init() throws ApiException {
		LOGGER.info("RulesEngine Initialization");
		//Fireable f = rulesLoader.getRuleTree("#25:0");
		LOGGER.info("Running Recommendation Engine");
	}

	@Scheduled(fixedDelay = 60000)
	public void scheduledMethod() throws ApiException {
		LOGGER.info("Running iteration");
		Fireable f = rulesLoader.getRuleTree("#25:0");
		measurements.updateLatest();
		f.fire();
    }
    //@Scheduled(fixedDelay = 1000)
    public void repeatingrule(){

	}

}
