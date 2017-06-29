import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.configuration.ExternalServicesConfiguration;
import it.cnit.gaia.rulesengine.service.ScheduleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.text.ParseException;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ExternalServicesConfiguration.class, ScheduleService.class})
public class ScheduleServiceTest {

	@Autowired
	ScheduleService scheduleService;

	@Test
	public void test0() throws ParseException, BuildingDatabaseException, IOException {
		scheduleService.updateSchedules();
		boolean occuipied = scheduleService.isOccupied(47L);
		System.out.println(occuipied);
	}

}
