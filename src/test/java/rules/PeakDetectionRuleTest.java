package rules;

import io.swagger.client.ApiException;
import it.cnit.gaia.rulesengine.api.exception.GaiaRuleException;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.rules.PeakDetectionRule;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import it.cnit.gaia.rulesengine.service.SparksService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparksService.class, MeasurementRepository.class})
public class PeakDetectionRuleTest extends GenericRuleTest {
	PeakDetectionRule rule;

	@Autowired
	SparksService sparksService;
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		rule = new PeakDetectionRule();
		School school = mock(School.class);
		when(school.getName()).thenReturn("DummySchool");
		rule.setSchool(school);
		rule.setWebsocket(websocketService);
		rule.setBuildingDBService(buildingDatabaseService);
		rule.setEventService(eventService);
		//rule.setMeasurements(measurementRepository);
		rule.setRuleDatabaseService(ruleDatabaseService);
		rule.setWeatherService(weatherService);
		rule.description = "description";
		rule.suggestion = "suggestion";
		rule.name = "name";
		sparksService.getMeterMap().put("155405",155405L);
		sparksService.requestAccessToken();
	}

	@Test
	public void test() throws ApiException, GaiaRuleException, RuleInitializationException {
		rule.power_uri = "155405";
		rule.init();
		rule.fire();
		Map<String, Object> allFields = rule.getAllFields();
		System.out.println(allFields);
	}

}
