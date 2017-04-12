package rules;

import it.cnit.gaia.rulesengine.configuration.ContextProvider;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.rules.DummyRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.Invocation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.Date;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ContextProvider.class, DummyRule.class})
public class DummyRuleTest extends GenericRuleTest {
	DummyRule rule;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		rule = new DummyRule();
		setUpRule(rule);
	}

	@Test
	public void testFailInitializationWithNullThreshold() {
		try {
			Assert.assertFalse(rule.init());
		} catch (RuleInitializationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testValidInitilialization() {
		rule.threshold = 1.0;
		try {
			Assert.assertTrue(rule.init());
		} catch (RuleInitializationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testInThresholdValue() {
		rule.threshold = 0.0;
		Assert.assertFalse(rule.condition());
	}

	@Test
	public void testOverThresholdValue() {
		rule.threshold = 1.0;
		Assert.assertTrue(rule.condition());
	}

	@Test
	public void testFire() {
		rule.threshold = 1.0;
		rule.fire();
	}

	@Test 	//Should NOT be fired
	public void testInsideInterval() {
		Date now = new Date();
		Date timestamp = new Date(now.getTime() - 10000); //minus 60 minutes
		when(eventService.getLatestEventTimestamp(anyString())).thenReturn(timestamp);
		rule.threshold = 1.0;
		rule.intervalInSeconds = 100L;
		rule.fire();
		Collection<Invocation> invocations = Mockito.mockingDetails(websocketService).getInvocations();
		Assert.assertEquals(invocations.size(),0);	}

	@Test	//Should be fired
	public void testOutsideInterval() {
		Date now = new Date();
		Date timestamp = new Date(now.getTime() - 3600000); //minus 1 hour
		when(eventService.getLatestEventTimestamp(anyString())).thenReturn(timestamp);
		rule.threshold = 1.0;
		rule.intervalInSeconds = 100L;
		rule.fire();
		//FIXME Other interactions
		Collection<Invocation> invocations = Mockito.mockingDetails(websocketService).getInvocations();
		Assert.assertEquals(invocations.size(),1);
	}

	protected void setUpRule(GaiaRule rule){
		School school = mock(School.class);
		when(school.getName()).thenReturn("DummySchool");
		rule.setSchool(school);
		rule.setWebsocket(websocketService);
		rule.setBuildingDBService(buildingDatabaseService);
		rule.setEventService(eventService);
		rule.setMeasurements(measurementRepository);
		rule.setGraphFactory(graphFactory);
		rule.description = "description";
		rule.suggestion = "suggestion";
		rule.name = "name";
	}

}
