package rules;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.weatherlibrary.WeatherService;
import io.swagger.sparks.model.SingleResourceMeasurementAPIModel;
import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.rulesengine.configuration.ContextProvider;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.service.*;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ContextProvider.class, GenericRuleTest.class})
public class GenericRuleTest {

	@Mock(answer = Answers.RETURNS_MOCKS)
	OrientGraphFactory graphFactory;
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
	@Mock
	MetadataService metadataService;
	@Mock
	MailService mailService;


	protected void setUpRule(GaiaRule rule){
		School school = mock(School.class);
		when(school.getName()).thenReturn("DummySchool");
		rule.setSchool(school);
		rule.setMetadataService(metadataService);
		rule.setMailService(mailService);
		rule.setWebsocket(websocketService);
		rule.setBuildingDBService(buildingDatabaseService);
		rule.setEventService(eventService);
		rule.setMeasurements(measurementRepository);
		rule.setRuleDatabaseService(ruleDatabaseService);
		rule.setWeatherService(weatherService);
		rule.description = "description";
		rule.suggestion = "suggestion";
		rule.name = "name";
	}

	protected void setMockValueForUri(String uri, Double value){
		SingleResourceMeasurementAPIModel resourceDataDTO = new SingleResourceMeasurementAPIModel();
		resourceDataDTO.setReading(value);
		when(measurementRepository.getLatestFor(uri)).thenReturn(resourceDataDTO);
	}

}
