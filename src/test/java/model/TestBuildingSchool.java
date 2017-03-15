package model;

import it.cnit.gaia.rulesengine.buildingdb.*;
import org.junit.Test;

import java.util.List;

public class TestBuildingSchool {
	@Test
	public void testAreas() throws BDBException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		List<AreaBDB> s =  bdbs.getAreas(27827L);
		System.out.println(s.toString());
	}

	@Test
	public void testArea() throws BDBException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		AreaBDB a =  bdbs.getAreaById(10L);
		System.out.println(a.toString());
	}

	@Test
	public void testBuildings() throws BDBException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		List<BuildingBDB> a =  bdbs.getBuildings();
		System.out.println(a.toString());
	}

	@Test
	public void testBuilding() throws BDBException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		BuildingBDB a =  bdbs.getBuildingById(27827L);
		System.out.println(a.toString());
	}

	@Test
	public void testGateways() throws BDBException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		List<GatewayBDB> a =  bdbs.getGateways(155076L);
		System.out.println(a.toString());
	}

	@Test
	public void testGateway() throws BDBException {
		BuildingDatabaseService bdbs = new BuildingDatabaseService();
		GatewayBDB a =  bdbs.getGatewayById(155076L);
		System.out.println(a.toString());
	}

}
