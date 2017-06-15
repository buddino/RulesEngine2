package model;

import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.buildingdb.dto.AreaDTO;
import it.cnit.gaia.buildingdb.dto.BuildingDTO;
import it.cnit.gaia.buildingdb.dto.GatewayDTO;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import org.junit.Test;

import java.util.List;

public class TestBuildingSchool {
	@Test
	public void testAreas() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		List<AreaDTO> s = bdbs.getAreas(27827L);
		System.out.println(s.toString());
	}

	@Test
	public void testArea() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		AreaDTO a = bdbs.getAreaById(10L);
		System.out.println(a.toString());
	}

	@Test
	public void testBuildings() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		List<BuildingDTO> a = bdbs.getBuildings();
		System.out.println(a.toString());
	}

	@Test
	public void testBuilding() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		BuildingDTO a = bdbs.getBuildingById(27827L);
		System.out.println(a.toString());
	}

	@Test
	public void testGateways() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		List<GatewayDTO> a = bdbs.getGateways(155076L);
		System.out.println(a.toString());
	}

	@Test
	public void testGateway() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		GatewayDTO a = bdbs.getGatewayById(155076L);
		System.out.println(a.toString());
	}

	@Test
	public void testStructure() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		BuildingDTO buildingDTO = bdbs.getBuildingStructure(155076L);
		System.out.println(buildingDTO.getChildren());
	}


}
