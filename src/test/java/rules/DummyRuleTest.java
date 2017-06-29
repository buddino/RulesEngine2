package rules;

import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.rules.DummyRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.Invocation;

import java.util.Collection;
import java.util.Date;

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
		rule.fireInterval =60L;
		rule.threshold = 1.0;
		try {
			Assert.assertTrue(rule.init());
		} catch (RuleInitializationException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testNoIntervalNoCron(){
		rule.threshold = 1.0;
		Assert.assertTrue(rule.isTriggeringIntervalValid());
	}

	@Test
	public void testValidIntervalNoCron() throws RuleInitializationException {
		rule.threshold = 1.0;
		rule.fireInterval = 60L;
		rule.init();
		Assert.assertTrue(rule.isTriggeringIntervalValid());
	}

	@Test
	public void testInvalidIntervalNoCron() throws RuleInitializationException {
		rule.threshold = 1.0;
		rule.fireInterval = 60L;
		rule.init();
		rule.fire();
		Assert.assertFalse(rule.isTriggeringIntervalValid());
	}

	@Test
	public void testNoIntervalInsideCron() throws RuleInitializationException {
		rule.threshold = 1.0;
		rule.fireCron = "* * * * * ? *";
		rule.init();
		rule.fire();
		Assert.assertTrue(rule.isTriggeringIntervalValid());
	}

	@Test(expected = RuleInitializationException.class)
	public void testNoIntervalInvalidCron() throws RuleInitializationException {
		rule.threshold = 1.0;
		rule.fireCron = "edfewfew";
		rule.init();
	}

	@Test
	public void testNoIntervalOutsideCron() throws RuleInitializationException {
		rule.threshold = 1.0;
		rule.fireCron = "1 * * * * ? 2010";
		rule.init();
		rule.fire();
		Assert.assertFalse(rule.isTriggeringIntervalValid());
	}

	@Test
	public void testInvialidIntervalInsideCron() throws RuleInitializationException {
		rule.threshold = 1.0;
		rule.fireInterval = 60L;
		rule.fireCron = "* * * * * ? *";
		rule.init();
		rule.fire();
		Assert.assertFalse(rule.isTriggeringIntervalValid());
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
		Date timestamp = new Date(now.getTime() - 10000); //minus 10 seconds
		rule.latestFireTime = timestamp;
		rule.threshold = 1.0;
		rule.fireInterval = 100L;
		rule.fire();
		Collection<Invocation> invocations = Mockito.mockingDetails(websocketService).getInvocations();
		Assert.assertEquals(invocations.size(),0);	}

	@Test	//Should be fired
	public void testOutsideInterval() {
		Date now = new Date();
		Date timestamp = new Date(now.getTime() - 3600000); //minus 1 hour
		rule.latestFireTime = timestamp;
		rule.threshold = 1.0;
		rule.fireInterval = 100L;
		rule.fire();
		//FIXME Other interactions
		Collection<Invocation> invocations = Mockito.mockingDetails(websocketService).getInvocations();
		Assert.assertEquals(invocations.size(),1);
	}


}
