package it.cnit.gaia.rulesengine.service;

import io.swagger.sparks.model.SingleResourceMeasurementAPIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class MicroOptimizationService {
	Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

	@Autowired
	MeasurementRepository measurements;
	@Autowired
	WebsocketService websocketService;

	/**
	 * Checks the latest measurements' timestamp available from each REQUIRED sensor
	 * If a measurement is elder than X
	 */
	//@Scheduled(initialDelay = 3600L, fixedDelay = 3600L)
	public void sensorCheck() {
		Long threshold = 3600 * 1000L;
		Date lastUpdate = measurements.getLastUpdate();
		Map<String, SingleResourceMeasurementAPIModel> latestReadings = measurements.getLatestReadings();
		Map<String, SingleResourceMeasurementAPIModel> sensorsWithOldData = latestReadings.entrySet()
																						  .stream()
																						  .filter(m -> (lastUpdate
																				.getTime() - m.getValue()
																							  .getTimestamp()) > threshold)
																						  .collect(Collectors
																				.toMap(a -> a.getKey(), a -> a
																						.getValue()));
		for( Entry<String, SingleResourceMeasurementAPIModel> entry : sensorsWithOldData.entrySet() ){
			Long id = measurements.getMeterMap().get(entry.getKey());
			StringBuilder stb = new StringBuilder();
			Date now = new Date();
			Long difference = now.getTime() - entry.getValue().getTimestamp();
			stb.append("Latest measurement ").append(difference/1000).append(" seconds ago.");
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			stb.append("(").append(sdf.format(entry.getValue().getTimestamp())).append(")");
			websocketService.pushSensorWarning(id, entry.getKey(), stb.toString());
			LOGGER.warn(stb.toString());
		}


	}
}
