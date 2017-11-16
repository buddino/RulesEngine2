package it.cnit.gaia.rulesengine;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.exception.OStorageException;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import io.swagger.sparks.ApiException;
import it.cnit.gaia.rulesengine.configuration.ContextProvider;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import it.cnit.gaia.rulesengine.service.MetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Service
public class Scheduler {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	MeasurementRepository measurements;
	@Autowired
	RulesLoader rulesLoader;
	@Autowired
	OrientGraphFactory ogf;
	@Autowired
	MetadataService metadataService;
	@Autowired
	ContextProvider contextProvider;

	Boolean isRunning = Boolean.FALSE;

	Collection<School> schools;

	@Value("${scheduler.interval}")
	String schedulerInterval;

	@Value("${scheduler.retry_interval_in_seconds}")
	Long retryInterval;

	@PostConstruct
	public void auth() throws IOException {
		measurements.getMeasurementService().checkAuth();
	}

	//@PostConstruct
	public void init() throws ApiException, IOException {

		//Set the thread pool size to one. In this way only one scheduled thread will be executed avoiding concurrency
		ThreadPoolTaskScheduler scheduler = (ThreadPoolTaskScheduler) ContextProvider.getBean(TaskScheduler.class);
		scheduler.setPoolSize(1);
		scheduler.setThreadNamePrefix("SCHEDULED");

		LOGGER.info("RulesEngine Initialization");
		LOGGER.info("Interval: " + schedulerInterval + "ms");
		//Test connection to the database
		LOGGER.info("Testing connection to the database");
		try {
			ODatabase.STATUS status = ogf.getDatabase().getStatus();
			LOGGER.debug("Database status: " + status.toString());
		} catch (OStorageException e) {
			LOGGER.error(e.getMessage());
		}

		LOGGER.info("Checking authentication");
		measurements.getMeasurementService().checkAuth();
		rulesLoader.loadSchools();
		LOGGER.info("Loading schedules");
		reloadSchedules();
		LOGGER.info("\nRecommendations Engine ready!\n");

	}

	//@Scheduled(fixedRateString = "${scheduler.interval}")
	public void scheduledMethod() throws IOException, InterruptedException {
		//Riguarda
		measurements.getMeasurementService().checkAuth();
		schools = rulesLoader.loadSchools().values();
		LOGGER.info("Executing iteration");
		if(!measurements.updateLatest()) {
			LOGGER.warn("No measurement have been updated. Retrying in "+retryInterval+" seconds");
			TimeUnit.SECONDS.sleep(retryInterval);
			if(!measurements.updateLatest())
				LOGGER.warn("Still can't get any measurement. Next iteration is scheduled.");
				return;
		}
		schools.forEach(s -> s.fire());
	}

	//@Scheduled(fixedDelay = 900 * 1000)
	public void reloadSchools() {
		rulesLoader.reloadAllSchools();
	}

	@Scheduled(cron = "0 0 12 * * ?")
	public void reloadSchedules() throws IOException {
		measurements.getMeasurementService().checkAuth();
		metadataService.updateAll();
	}


}
