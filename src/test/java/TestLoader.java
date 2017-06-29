import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import com.weatherlibrary.WeatherService;
import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.rulesengine.api.exception.GaiaRuleException;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.rules.ScheduledReminderRule;
import it.cnit.gaia.rulesengine.service.EventService;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import it.cnit.gaia.rulesengine.service.RuleDatabaseService;
import it.cnit.gaia.rulesengine.service.WebsocketService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RulesLoader.class, OrientConfiguration.class})
public class TestLoader {
	ScheduledReminderRule rule;

	@Mock
	WebsocketService websocketService;
	@Mock
	EventService eventService;
	@Mock
	MeasurementRepository measurementRepository;
	@Mock
	BuildingDatabaseService buildingDatabaseService;
	@Mock
	RuleDatabaseService ruleDatabaseService;
	@Mock
	WeatherService weatherService;

	@Autowired
	RulesLoader loader;

	@Autowired
	OrientGraphFactory ogf;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test() throws GaiaRuleException {
		OrientVertex vertex = ogf.getNoTx().getVertex("32:0");
		ScheduledReminderRule rule = (ScheduledReminderRule) loader.getRuleForTest(vertex);
		System.out.println(rule);
	}


}
