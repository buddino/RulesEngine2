package rules;

import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.intervalparser.LocalInterval;
import it.cnit.gaia.intervalparser.LocalIntervalParser;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.rules.HolidayShutdown;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;
import java.util.Arrays;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

public class TestHolidayShutdown extends GenericRuleTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	HolidayShutdown rule;
	DateTime dateTime = DateTime.now();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		rule = new HolidayShutdown();
		setUpRule(rule);
	}

	@Test
	public void testConditionVerified() throws BuildingDatabaseException, RuleInitializationException, ParseException {
		/*
		The event is set 3 days from now, the notification is set 96 hours before the event (4 days)
		So the condition should be true
		 */
		DateTime futureDate = this.dateTime.plusDays(3);
		String interval = String.format("%02d/%02d/%d-31/12/2020",futureDate.getDayOfMonth(),futureDate.getMonthOfYear(),futureDate.getYear());
		LocalIntervalParser localIntervalParser = new LocalIntervalParser();
		LocalInterval parse = localIntervalParser.parse(interval);
		when(metadataService.getClosed(anyLong())).thenReturn(Arrays.asList(parse));
		rule.timeBeforeInHours = 96L;
		rule.init();
		Assertions.assertTrue(rule.condition());
	}

	@Test
	public void testConditionNotVerified() throws BuildingDatabaseException, RuleInitializationException, ParseException {
		/*
		The event is set 3 days from now, the notification is set to 36 hours before the event
		So the condition should be false
		 */
		DateTime futureDate = this.dateTime.plusDays(3);
		String interval = String.format("%02d/%02d/%d-31/12/2020",futureDate.getDayOfMonth(),futureDate.getMonthOfYear(),futureDate.getYear());
		LocalIntervalParser localIntervalParser = new LocalIntervalParser();
		LocalInterval parse = localIntervalParser.parse(interval);
		when(metadataService.getClosed(anyLong())).thenReturn(Arrays.asList(parse));
		rule.timeBeforeInHours = 36L;
		rule.init();
		Assertions.assertFalse(rule.condition());
	}


}