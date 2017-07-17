package service;

import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.service.MetadataService;
import org.codehaus.jackson.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BuildingDatabaseService.class})
public class MetadataServiceTest {
	MetadataService metadataService = new MetadataService();

	@Autowired
	BuildingDatabaseService bds;

	@Before
	public void setup(){
		ReflectionTestUtils.setField(metadataService,"bds", bds);
	}

	@Test
	public void test() throws BuildingDatabaseException, IOException {
		JsonNode width = metadataService.getJsonFieldForArea(42L, "width");
		System.out.println(width);
	}
}
