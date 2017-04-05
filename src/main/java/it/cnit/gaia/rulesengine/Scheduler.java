package it.cnit.gaia.rulesengine;

import io.swagger.client.ApiException;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.School;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class Scheduler {
	private final Logger LOGGER = Logger.getLogger(Scheduler.class.getSimpleName());


	@Autowired
	MeasurementRepository measurements;
	@Autowired
	RulesLoader rulesLoader;

	Collection<School> schools;

	//@PostConstruct
	public void init() throws ApiException {
		LOGGER.info("RulesEngine Initialization");
		schools = rulesLoader.loadSchools().values();
		measurements.updateLatest();
		schools.forEach(s -> s.fire());
	}

	@Scheduled(fixedDelayString = "${scheduler.interval}")
	public void scheduledMethod() throws ApiException {
		rulesLoader.reloadAllSchools();
		schools = rulesLoader.loadSchools().values();
		measurements.updateLatest();
		schools.forEach(s -> s.fire());
	}
}
