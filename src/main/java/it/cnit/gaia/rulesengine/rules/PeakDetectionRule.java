package it.cnit.gaia.rulesengine.rules;

import io.swagger.sparks.ApiException;
import io.swagger.sparks.model.ResourceAnalyticsDataResponseAPIModel;
import io.swagger.sparks.model.SingleResourceMeasurementAPIModel;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.utils.PeakDetector;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static io.swagger.sparks.model.ResourceQueryCriteriaRequestWithinATimeframeAPIModel.GranularityEnum;

public class PeakDetectionRule extends GaiaRule {
	@URI
	public String power_uri;

	@LogMe
	public List<Double> peaks_values = new ArrayList<>();
	@LogMe
	public List<Long> peaks_times = new ArrayList<>();

	public boolean condition() {
		detectPeaks();
		return peaks_values.size() != 0;
	}


	public void detectPeaks() {
		//Create timestamp for yesterday at midnight and today at midnigh
		LocalDateTime today = LocalDate.now().atStartOfDay().minusDays(1);
		LocalDateTime yesterday = LocalDate.now().atStartOfDay().minusDays(2);
		Long from = yesterday.toInstant(ZoneOffset.ofHours(2)).toEpochMilli();
		Long to = today.toInstant(ZoneOffset.ofHours(2)).toEpochMilli();
		//Query the measurements with a 5 minutes granularity
		ResourceAnalyticsDataResponseAPIModel response = null;
		try {
			response = measurements
					.getTimeRange(power_uri, from, to, GranularityEnum._5MIN);
		} catch (ApiException e) {
			error(e.getMessage());
			return;
		}
		if (response == null) {
			error("Error while retrieving the measurements");
			return;
		}
		//Compute peaks_values
		double[] data = responseToDoubleArray(response);
		PeakDetector peakDetector = new PeakDetector(data);
		peakDetector.removeAnomalies().detectPeaks();
		List<Integer> peakIndeces = peakDetector.getPeakIndeces();
		peaks_values = peakDetector.getPeaks();
		for (Integer index : peakIndeces) {
			Long timestamp = response.getData().get(index).getTimestamp();
			peaks_times.add(timestamp);
		}
		/*
		LOGGER.debug(String.valueOf(from));
		LOGGER.debug(String.valueOf(to));
		LOGGER.debug("Found: "+peaks_values.size()+" peaks_values");
		for(Integer index : peakIndeces) {
			Long timestamp = response.getData().get(index).getTimestamp();
			LOGGER.debug("Index: "+ index);
			LOGGER.debug("Value: "+ peaks_values.get(0));
			LOGGER.debug(sdf.format(new Date(timestamp)));
		}
		*/
	}

	private double[] responseToDoubleArray(ResourceAnalyticsDataResponseAPIModel response) {
		List<SingleResourceMeasurementAPIModel> data = response.getData();
		int size = data.size();
		double[] out = new double[size];
		for (int i = 0; i < size; i++) {
			out[i] = data.get(i).getReading();
		}
		return out;
	}

}
