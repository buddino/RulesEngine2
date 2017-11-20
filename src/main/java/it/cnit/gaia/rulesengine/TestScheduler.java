package it.cnit.gaia.rulesengine;

import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
@Profile("noschedule")
public class TestScheduler {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	MeasurementRepository measurements;
	//Only authenticate
	@PostConstruct
	public void auth() throws IOException {
		LOGGER.warn("USING TEST PROFILE");
		measurements.getMeasurementService().checkAuth();
	}

}
