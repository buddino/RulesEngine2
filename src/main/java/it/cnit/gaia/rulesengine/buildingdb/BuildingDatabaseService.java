package it.cnit.gaia.rulesengine.buildingdb;

import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//TODO Make API
public class BuildingDatabaseService {
	RestTemplate restTemplate = new RestTemplate();
	final String baseURL = "http://150.140.5.64:8080/GaiaAnalytics/gaia/";

	public BuildingBDB getBuildingById(Long id) throws BDBException {
		String uri = baseURL+"utility/buildings/getbyid/{id}";
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(id));
		BuildingByIdResultBDB result = restTemplate.getForObject(uri, BuildingByIdResultBDB.class, params);
		if(result.getResult().equals("KO"))
			throw new BDBException(result.getError());
		return result.getItem();
	}
	public List<BuildingBDB> getBuildings() throws BDBException {
		String uri = baseURL+"utility/buildings/getall";
		AllBuildingsResultBDB result = restTemplate.getForObject(uri, AllBuildingsResultBDB.class);
		if(result.getResult().equals("KO"))
			throw new BDBException(result.getError());
		return result.getItems();
	}

	public List<AreaBDB> getAreas(Long buildingId) throws BDBException {
		String uri = baseURL+"utility/areas/getall/{id}";
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(buildingId));
		AllAreasResultBDB result = restTemplate.getForObject(uri, AllAreasResultBDB.class, params);
		if(result.getResult().equals("KO"))
			throw new BDBException(result.getError());
		return result.getItems();
	}

	public AreaBDB getAreaById(Long areaId) throws BDBException {
		String uri = baseURL+"utility/areas/getbyid/{id}";
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(areaId));
		AreaByIdResultBDB result = restTemplate.getForObject(uri, AreaByIdResultBDB.class, params);
		if(result.getResult().equals("KO"))
			throw new BDBException(result.getError());
		return result.getItem();
	}

	public List<GatewayBDB> getGateways(Long buildingId) throws BDBException {
		String uri = baseURL+"utility/gateways/getbyid/{id}";
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(buildingId));
		AllGatewaysResultBDB result = restTemplate.getForObject(uri, AllGatewaysResultBDB.class, params);
		if(result.getResult().equals("KO"))
			throw new BDBException(result.getError());
		return result.getItems();
	}

	public GatewayBDB getGatewayById(Long gwId) throws BDBException {
		String uri = baseURL+"utility/gateways/getbyid/{id}";
		Map<String, String> params = new HashMap<>();
		params.put("id", String.valueOf(gwId));
		GatewayByIdResultBDB result = restTemplate.getForObject(uri, GatewayByIdResultBDB.class, params);
		if(result.getResult().equals("KO"))
			throw new BDBException(result.getError());
		return result.getItem();
	}



}
