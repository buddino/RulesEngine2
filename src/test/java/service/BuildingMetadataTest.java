package service;

import it.cnit.gaia.api.MetadataAPI;
import it.cnit.gaia.api.model.Schedule;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.service.MetadataService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Date;

import static org.mockito.Mockito.mock;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparksTokenRequest.class, MetadataAPI.class})
public class BuildingMetadataTest {

	private RulesLoader rulesLoader;

	MetadataService metaDataService;
	@Autowired
	SparksTokenRequest sparksTokenRequest;
	@Autowired
	MetadataAPI metadataAPI;

	@Before
	public void setup(){
		rulesLoader = mock(RulesLoader.class);
		metaDataService = new MetadataService();
		ReflectionTestUtils.setField(metaDataService, "rulesLoader", rulesLoader);
		ReflectionTestUtils.setField(metaDataService, "tokenRequest", sparksTokenRequest);
		ReflectionTestUtils.setField(metaDataService, "metadataAPI", metadataAPI);
		metaDataService.init();
		metaDataService.forceTokenRefresh();
	}

	@Test
	public void metaData(){
		Collection<Schedule> schedules = metaDataService.getSchedules(155076L);
		System.out.println(schedules);
	}

	@Test
	public void testIsClosed() throws BuildingDatabaseException {
		boolean closed = metaDataService.isClosed(155076L, new Date(1502748000000L));
		System.out.println(closed);
		boolean open = metaDataService.isClosed(155076L, new Date(1491379200000L));
		System.out.println(open);
	}
}
