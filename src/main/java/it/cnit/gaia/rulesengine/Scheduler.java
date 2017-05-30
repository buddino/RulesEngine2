package it.cnit.gaia.rulesengine;

import com.orientechnologies.orient.core.db.ODatabase;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import io.swagger.client.ApiException;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;

@Service
public class Scheduler {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	MeasurementRepository measurements;
	@Autowired
	RulesLoader rulesLoader;
	@Autowired
	OrientGraphFactory ogf;

	Collection<School> schools;

	@Value("${scheduler.interval}")
	String schedulerInterval;


	@PostConstruct
	public void init() throws ApiException {
		LOGGER.info("RulesEngine Initialization");
		LOGGER.info("Interval: "+schedulerInterval +"ms");
		//Test connection to the database
		LOGGER.info("Testing connection to the database");
		ODatabase.STATUS status = ogf.getDatabase().getStatus();
		LOGGER.debug("Database status: "+status.toString());
	}

	@Scheduled(fixedDelayString = "${scheduler.interval}")
	public void scheduledMethod() {
		//Riguarda
		measurements.getMeasurementService().checkAuth();

		LOGGER.info("Executing iteration");
		//rulesLoader.reloadAllSchools();
		schools = rulesLoader.loadSchools().values();
		measurements.updateLatest();
		schools.forEach(s -> s.fire());
	}

}
