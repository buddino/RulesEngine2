package service;


import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import io.swagger.sparks.ApiException;
import io.swagger.sparks.model.ResourceAPIModel;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.Area;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.model.annotation.URI;
import it.cnit.gaia.rulesengine.service.MeasurementRepository;
import it.cnit.gaia.rulesengine.service.SparksService;
import it.cnit.gaia.rulesengine.service.RuleCreationHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparksService.class, SparksTokenRequest.class, RuleCreationHelper.class, OrientConfiguration.class, RulesLoader.class, MeasurementRepository.class})
public class RuleCreationHelperTest {
	private final String rulesPackage = "it.cnit.gaia.rulesengine.rules";

	@Autowired
	RuleCreationHelper helper;
	@Autowired
	SparksService sparks;
	@Autowired
	OrientGraphFactory ogf;
	@Autowired
	RulesLoader rulesLoader;


	@Before
	public void setup() {
		sparks.forceTokenRefresh();
	}

	@Test
	public void testRuleSuggestion() throws ApiException {
		String property = "Luminosity";
		ResourceAPIModel suggestedResource = helper.getSuggestedResourceByProperty(property, 159742L, null);
		System.out.println(suggestedResource);
	}

	@Test
	public void testRule() throws ClassNotFoundException, ApiException {
		Long room_55 = 159737L;
		String classname = "ComfortIndex";
		Class<?> aClass = Class.forName(rulesPackage + "." + classname);
		Field[] fields = aClass.getFields();
		List<String> uris = new ArrayList<>();
		for (Field f : fields) {
			if (f.isAnnotationPresent(URI.class))
				uris.add(f.getName());
		}
		for (String uri : uris) {
			ResourceAPIModel suggestedResourceByUri = helper.getSuggestedResource(uri, room_55, null);
			System.out.println(uri + ": " + suggestedResourceByUri);
		}
	}

	@Test
	public void testDefaults() throws ApiException {
		Long room_55 = 159737L;
		Area r55 = new Area();
		r55.aid = room_55;
		School s = new School();
		s.aid = 155076L;
		r55.setSchool(s);
		rulesLoader.getAreaMap().put(room_55,r55);
		ResourceAPIModel suggestedResourceByUri = helper.getSuggestedResource("root_temperature_uri", room_55, null);
		System.out.println(suggestedResourceByUri);
	}
}