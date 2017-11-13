package service;


import io.swagger.sparks.ApiException;
import io.swagger.sparks.model.ResourceAPIModel;
import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import it.cnit.gaia.rulesengine.service.SparksService;
import it.cnit.gaia.rulesengine.utils.RuleCreateHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparksService.class, SparksTokenRequest.class, RuleCreateHelper.class})
public class RuleCreateHelperTest {
	@Autowired
	RuleCreateHelper helper;
	@Autowired
	SparksService sparks;

	@Before
	public void setup() {
		sparks.forceTokenRefresh();
	}

	@Test
	public void testRuleSuggestion() throws ApiException {
		String property = "Luminosity";
		ResourceAPIModel suggestedResource = helper.getSuggestedResource(property, 159742L);
		System.out.println(suggestedResource);
	}
}