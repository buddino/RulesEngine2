package it.cnit.gaia.rulesengine.utils;


import io.swagger.sparks.model.SingleResourceMeasurementAPIModel;

import java.util.Date;

public class MeasurementsUtils {

	public static SingleResourceMeasurementAPIModel getResourceDTO(Double value){
		SingleResourceMeasurementAPIModel dto = new SingleResourceMeasurementAPIModel();
		dto.setReading(value);
		dto.setTimestamp(new Date().getTime());
		return dto;
	}
}
