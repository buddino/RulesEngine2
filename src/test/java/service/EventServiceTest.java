package service;

import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import it.cnit.gaia.rulesengine.service.RuleDatabaseService;
import it.cnit.gaia.rulesengine.service.StatsService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OrientConfiguration.class})
public class EventServiceTest {

	StatsService statSer;

	@Autowired
	OrientGraphFactory graphFactory;

	@Before
	public void setup(){
		statSer = new StatsService();
		RuleDatabaseService ruleDatabaseService = mock(RuleDatabaseService.class);
		ReflectionTestUtils.setField(statSer, "ruleDatabaseService", ruleDatabaseService);
		ReflectionTestUtils.setField(statSer, "graphFactory", graphFactory);
	}



}