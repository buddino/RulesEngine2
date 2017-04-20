import it.cnit.gaia.buildingdb.BuildingDatabaseException;
import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.buildingdb.ScheduleDTO;
import it.cnit.gaia.rulesengine.configuration.BuildingDBConfiguration;
import it.cnit.gaia.rulesengine.service.ScheduleService;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.util.Arrays;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BuildingDBConfiguration.class, ScheduleService.class})
public class ScheduleServiceTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	BuildingDatabaseService bds;

	@InjectMocks
	ScheduleService scheduleService;

	@Test
	public void testNotVaidCron() throws BuildingDatabaseException, ParseException {
		thrown.expect(ParseException.class);
		ScheduleDTO schedule = new ScheduleDTO();
		schedule.setCronstrs(Arrays.asList("* * * d-25 4 ? 2016"));
		when(bds.getScheduleForArea(anyLong())).thenReturn(schedule);
		Assert.assertFalse(scheduleService.isInInterval(123456L));
	}

	@Test
	public void testPassedEvent() throws BuildingDatabaseException, ParseException {
		ScheduleDTO schedule = new ScheduleDTO();
		schedule.setCronstrs(Arrays.asList("* * * 20-25 4 ? 2016"));
		when(bds.getScheduleForArea(anyLong())).thenReturn(schedule);
		Assert.assertFalse(scheduleService.isInInterval(123456L));
	}

	@Test
	public void testFutureEvent() throws BuildingDatabaseException, ParseException {
		ScheduleDTO schedule = new ScheduleDTO();
		DateTime now = DateTime.now();
		DateTime future = now.plusDays(1);
		String cron = String.format(
				"* * * %d %d ? %d",
				future.getDayOfMonth(),
				future.getMonthOfYear(),
				future.getYear());
		schedule.setCronstrs(Arrays.asList(cron));
		when(bds.getScheduleForArea(anyLong())).thenReturn(schedule);
		Assert.assertFalse(scheduleService.isInInterval(123456L));
	}

	@Test
	public void testInPeriod() throws BuildingDatabaseException, ParseException {
		ScheduleDTO schedule = new ScheduleDTO();
		DateTime now = DateTime.now();
		String cron = String.format(
				"* * * %d %d ? %d",
				now.getDayOfMonth(),
				now.getMonthOfYear(),
				now.getYear());
		schedule.setCronstrs(Arrays.asList(cron));		when(bds.getScheduleForArea(anyLong())).thenReturn(schedule);
		Assert.assertTrue(scheduleService.isInInterval(123456L));
	}

	@Test
	public void testMultipleCronInside() throws BuildingDatabaseException, ParseException {
		ScheduleDTO schedule = new ScheduleDTO();
		DateTime now = DateTime.now();
		String cron = String.format(
				"* * * %d %d ? %d",
				now.getDayOfMonth(),
				now.getMonthOfYear(),
				now.getYear());
		schedule.setCronstrs(Arrays.asList(cron, "* * * 15-20 4 ? 2016", "* * * 25 5 ? 2017"));		when(bds.getScheduleForArea(anyLong())).thenReturn(schedule);
		Assert.assertTrue(scheduleService.isInInterval(123456L));
	}

	@Test
	public void testMultipleCronOutside() throws BuildingDatabaseException, ParseException {
		ScheduleDTO schedule = new ScheduleDTO();
		DateTime now = DateTime.now();
		String cron = String.format(
				"* * * %d %d ? %d",
				now.getDayOfMonth()+1,
				now.getMonthOfYear(),
				now.getYear());
		schedule.setCronstrs(Arrays.asList(cron, "* * * 15-20 4 ? 2016", "* * * 25 5 ? 2016"));		when(bds.getScheduleForArea(anyLong())).thenReturn(schedule);
		Assert.assertFalse(scheduleService.isInInterval(123456L));
	}


}
