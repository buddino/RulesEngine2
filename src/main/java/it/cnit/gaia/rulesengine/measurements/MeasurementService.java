package it.cnit.gaia.rulesengine.measurements;

import io.swagger.client.ApiException;
import io.swagger.client.model.ResourceDataDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface MeasurementService {
	Map<String, ResourceDataDTO> queryLatest() throws ApiException;

	Map<String, List<ResourceDataDTO>> queryLatestHour() throws ApiException;

	Long uri2id(String uri) throws ApiException;

	Map<String, Long> getMeterMap();

	MeasurementService setMeterMap(Map<String, Long> meterMap);
}
