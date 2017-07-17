package rules;

import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.rules.CO2Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;

public class CO2LevelTest extends GenericRuleTest {
	CO2Level rule;

	@Rule
	public ExpectedException thrown = ExpectedException.none();


	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		rule = new CO2Level();
		rule.suggestion = "";
		setUpRule(rule);
	}

	@Test
	public void uriNotPresentTest() throws RuleInitializationException {
		thrown.expect(RuleInitializationException.class);
		setMockValueForUri("co2", 1250.0);
		rule.init();
	}

	@Test
	public void simpleTriggeredTest() throws RuleInitializationException {
		rule.co2_uri = "co2";
		setMockValueForUri("co2", 1250.0);
		rule.init();
		Assert.assertTrue(rule.condition());
	}

	@Test
	public void testWithTemperatureDiff() throws RuleInitializationException {
		rule.co2_uri = "co2";
		rule.in_temp_uri = "in";
		rule.out_temp_uri = "out";
		setMockValueForUri("co2", 1250.0);
		setMockValueForUri("out", 35.0);
		setMockValueForUri("in", 20.0);
		rule.init();
		Assert.assertTrue(rule.condition());
	}

	@Test
	public void testWithTemperatureBelowDiff() throws RuleInitializationException {
		rule.co2_uri = "co2";
		rule.in_temp_uri = "in";
		rule.out_temp_uri = "out";
		setMockValueForUri("co2", 800.0);
		setMockValueForUri("out", 20.0);
		setMockValueForUri("in", 18.0);
		rule.init();
		Assert.assertFalse(rule.condition());
	}
}
