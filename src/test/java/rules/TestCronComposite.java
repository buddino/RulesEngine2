package rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.rules.CronComposite;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.when;

public class TestCronComposite extends GenericRuleTest {
	@Mock
	GaiaRule gaiaRule;
	CronComposite rule;
	DateTime dateTime = DateTime.now();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws RuleInitializationException {
		MockitoAnnotations.initMocks(this);
		rule = new CronComposite();
		setUpRule(rule);
		when(gaiaRule.init()).thenReturn(true);
		Set<GaiaRule> set = new HashSet<>();
		set.add(gaiaRule);
		rule.ruleSet = set;
	}

	@Test
	public void testInit() throws RuleInitializationException {
		rule.cronstrs = Arrays.asList("* * 15 * * ? *");
		rule.init();
	}

	@Test
	public void testNotValidString() throws RuleInitializationException {
		thrown.expect(RuleInitializationException.class);
		rule.cronstrs = Arrays.asList("* * 15 * * ? *","* * * 55 * * * * * *");
		rule.init();
	}

	@Test
	public void testMultipleString() throws RuleInitializationException {
		String str1 = String.format("* * %d * * ? *", dateTime.getHourOfDay());
		rule.cronstrs = Arrays.asList("* * * 5-10 1-12 ? 2014","* * 16-17 * 4-5 ? 2015", str1);
		rule.init();
		Assert.assertTrue(rule.condition());
	}

	@Test
	public void testNegative() throws RuleInitializationException {
		rule.cronstrs = Arrays.asList("* * * 5-10 1-12 ? 2014","* * 16-17 * 4-5 ? 2015");
		rule.negative = true;
		rule.init();
		Assert.assertTrue(rule.condition());
	}

	@Test
	public void testNegativeFalse() throws RuleInitializationException {
		String str1 = String.format("* * %d * * ? *", dateTime.getHourOfDay());
		rule.cronstrs = Arrays.asList("* * * 5-10 1-12 ? 2014","* * 16-17 * 4-5 ? 2015", str1);
		rule.negative = true;
		rule.init();
		Assert.assertFalse(rule.condition());
	}

}
