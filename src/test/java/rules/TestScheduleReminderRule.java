package rules;

import it.cnit.gaia.buildingdb.BuildingDatabaseException;
import it.cnit.gaia.buildingdb.ScheduleDTO;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import it.cnit.gaia.rulesengine.rules.ScheduleReminderRule;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.Invocation;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestScheduleReminderRule extends GenericRuleTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	ScheduleReminderRule rule;
	DateTime dateTime = DateTime.now();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		rule = new ScheduleReminderRule();
		setUpRule(rule);
	}

	@Test
	public void failInitNoTimeBefore() throws BuildingDatabaseException, RuleInitializationException {
		thrown.expect(RuleInitializationException.class);
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCronstrs(Arrays.asList("* * 10-15 * * ? *", "* * * 15 * ? *"));
		when(buildingDatabaseService.getScheduleForArea(anyLong())).thenReturn(scheduleDTO);
		rule.init();
	}

	@Test
	public void failInitNotValidCron() throws BuildingDatabaseException, RuleInitializationException {
		thrown.expect(RuleInitializationException.class);
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCronstrs(Arrays.asList("* * 10-15 * * ? *", "* * * p * ? *"));
		when(buildingDatabaseService.getScheduleForArea(anyLong())).thenReturn(scheduleDTO);
		rule.init();
	}

	@Test
	public void testConditionVerified() throws BuildingDatabaseException, RuleInitializationException {
		/*
		The event is set 3 days from now, the notification is set 96 hours before the event (4 days)
		So the condition should be true
		 */
		DateTime futureDate = this.dateTime.plusDays(3);
		String cron = String.format("* * * %d %d ? *", futureDate.getDayOfMonth(), futureDate.getMonthOfYear());
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCronstrs(Arrays.asList(cron));
		rule.timeBeforeInHours = 96L;
		when(buildingDatabaseService.getScheduleForArea(anyLong())).thenReturn(scheduleDTO);
		rule.init();
		Assert.isTrue(rule.condition());
	}

	@Test
	public void testConditionNotVerified() throws BuildingDatabaseException, RuleInitializationException {
		/*
		The event is set 3 days from now, the notification is set to 36 hours before the event
		So the condition should be false
		 */
		DateTime futureDate = this.dateTime.plusDays(3);
		String cron = String.format("* * * %d %d ? *", futureDate.getDayOfMonth(), futureDate.getMonthOfYear());
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCronstrs(Arrays.asList(cron));
		rule.timeBeforeInHours = 36L;
		when(buildingDatabaseService.getScheduleForArea(anyLong())).thenReturn(scheduleDTO);
		rule.init();
		Assertions.assertFalse(rule.condition());
	}

	@Test
	public void testOldEvent() throws BuildingDatabaseException, RuleInitializationException {
		/*

		 */
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCronstrs(Arrays.asList("* * * * * ? 2016"));
		rule.timeBeforeInHours = 36L;
		when(buildingDatabaseService.getScheduleForArea(anyLong())).thenReturn(scheduleDTO);
		rule.init();
		Assertions.assertFalse(rule.condition());
	}

	@Test
	public void testEmptyExpressionList() throws BuildingDatabaseException, RuleInitializationException {
		thrown.expect(RuleInitializationException.class);
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		rule.timeBeforeInHours = 36L;
		when(buildingDatabaseService.getScheduleForArea(anyLong())).thenReturn(scheduleDTO);
		rule.init();
		Assertions.assertFalse(rule.condition());
	}

	@Test
	public void test() throws BuildingDatabaseException, RuleInitializationException {
		DateTime futureDate = this.dateTime.plusDays(3);
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCronstrs(Arrays.asList("* * * "+futureDate.getDayOfMonth()+" "+futureDate.getMonthOfYear()+" ? *"));
		rule.timeBeforeInHours = 300L;
		when(buildingDatabaseService.getScheduleForArea(anyLong())).thenReturn(scheduleDTO);
		rule.init();
		rule.fire();
		Collection<Invocation> invocations = Mockito.mockingDetails(websocketService).getInvocations();
		verify(websocketService,times(1)).pushNotification((GAIANotification) anyObject());
	}


	@Test
	public void testLatestFireTime() throws BuildingDatabaseException, RuleInitializationException {
		DateTime futureDate = this.dateTime.plusDays(3);
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCronstrs(Arrays.asList("* * * "+futureDate.getDayOfMonth()+" "+futureDate.getMonthOfYear()+" ? *"));
		rule.timeBeforeInHours = 300L;
		when(buildingDatabaseService.getScheduleForArea(anyLong())).thenReturn(scheduleDTO);
		rule.init();
		rule.fire();
		Assertions.assertNotNull(rule.latestFireTime);
	}

	@Test
	public void testConsecutiveFire() throws BuildingDatabaseException, RuleInitializationException {
		DateTime futureDate = this.dateTime.plusDays(3);
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCronstrs(Arrays.asList("* * * "+futureDate.getDayOfMonth()+" "+futureDate.getMonthOfYear()+" ? *"));
		rule.timeBeforeInHours = 300L;
		when(buildingDatabaseService.getScheduleForArea(anyLong())).thenReturn(scheduleDTO);
		rule.intervalInSeconds = 100L;
		rule.init();
		rule.fire();
		rule.fire();
		verify(websocketService,times(1)).pushNotification((GAIANotification) anyObject());
	}

}