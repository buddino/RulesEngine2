package service;

import io.swagger.sparks.ApiException;
import io.swagger.sparks.api.ResourceAPIApi;
import io.swagger.sparks.api.SiteAPIApi;
import io.swagger.sparks.model.CollectionOfResourceAPIModel;
import io.swagger.sparks.model.ResourceAPIModel;
import io.swagger.sparks.model.SiteAPIModel;
import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import it.cnit.gaia.rulesengine.service.SparksService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparksService.class, SparksTokenRequest.class})
public class SparksServiceTest {
	@Autowired
	SparksService sparks;

	SiteAPIApi siteApi;
	ResourceAPIApi resourceAPIApi;


	@Before
	public void setup(){
		sparks.forceTokenRefresh();
		siteApi = sparks.getSiteApi();
		resourceAPIApi = sparks.getResApi();
	}

	@Test
	public void testsiteApi() throws ApiException {
		List<SiteAPIModel> subsites = siteApi.retrieveSubSites(155076L).getSites();
		System.out.println(subsites);
	}

	@Test
	public void testProperty() throws ApiException {
		String property = "Relative Humidity";
		CollectionOfResourceAPIModel collectionOfResourceAPIModel = resourceAPIApi.retrieveSiteResources(155076L);

		List<ResourceAPIModel> filtered = collectionOfResourceAPIModel.getResources().stream()
																	 .filter(r -> r.getProperty().equals(property))
																	 .collect(Collectors.toList());
		System.out.println(filtered);

		//Something like shortest URI
	}

}
