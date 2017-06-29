package rules;

import io.swagger.client.model.ResourceDataDTO;
import it.cnit.gaia.rulesengine.rules.ExpressionRule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class ExpressionRuleTest extends GenericRuleTest {

	private ExpressionRule rule;

	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		rule = new ExpressionRule();
		setUpRule(rule);	}

	@Test
	public void testParsingExpression(){
		when(measurementRepository.getLatestFor("param_uri")).thenReturn(new ResourceDataDTO().reading(10.0));
		rule.expression = "param_value < 15.0";
		rule.init();
		System.out.println(rule.condition());
	}

}
