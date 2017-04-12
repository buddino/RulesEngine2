package model;

import it.cnit.gaia.buildingdb.*;
import org.junit.Test;

import java.util.List;

public class TestBuildingSchool {
	@Test
	public void testAreas() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseServiceImpl();
		List<AreaDTO> s = bdbs.getAreas(27827L);
		System.out.println(s.toString());
	}

	@Test
	public void testArea() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseServiceImpl();
		AreaDTO a = bdbs.getAreaById(10L);
		System.out.println(a.toString());
	}

	@Test
	public void testBuildings() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseServiceImpl();
		List<BuildingDTO> a = bdbs.getBuildings();
		System.out.println(a.toString());
	}

	@Test
	public void testBuilding() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseServiceImpl();
		BuildingDTO a = bdbs.getBuildingById(27827L);
		System.out.println(a.toString());
	}

	@Test
	public void testGateways() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseServiceImpl();
		List<GatewayDTO> a = bdbs.getGateways(155076L);
		System.out.println(a.toString());
	}

	@Test
	public void testGateway() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseServiceImpl();
		GatewayDTO a = bdbs.getGatewayById(155076L);
		System.out.println(a.toString());
	}

	@Test
	public void testStructure() throws BuildingDatabaseException {
		BuildingDatabaseService bdbs = new BuildingDatabaseServiceImpl();
		BuildingDTO buildingDTO = bdbs.getBuildingStructure(155076L);
		System.out.println(buildingDTO.getChildren());
	}


}
