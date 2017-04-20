package it.cnit.gaia.rulesengine.utils;

import io.swagger.client.model.ResourceDataDTO;

import java.util.Date;

public class MeasurementsUtils {

	public static ResourceDataDTO getResourceDTO(Double value){
		ResourceDataDTO dto = new ResourceDataDTO();
		dto.setReading(value);
		dto.setTimestamp(new Date().getTime());
		return dto;
	}
}
