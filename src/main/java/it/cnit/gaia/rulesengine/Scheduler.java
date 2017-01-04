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
    private final Logger LOGGER = Logger.getRootLogger();

    @Autowired
    MeasurementRepository measurements;
    @Autowired
	RulesLoader rulesLoader;

    @PostConstruct
	public void init(){
    	measurements.updateLatest();
		Fireable f = rulesLoader.createRulesTree("#25:1");
		f.fire();
	}

    @Scheduled(fixedDelay = 9000000)
    public void scheduledMethod() throws ApiException {
		System.out.println("Scheduled event");
		//measurements.updateLatest();
		//ComfortIndex rule = new ComfortIndex().setHumidUri("0013a2004091d30c/0xd17/hih4030").setTempUri("0013a2004091d30c/0xd17/lm35");
		//rule.fire();
    }

}
