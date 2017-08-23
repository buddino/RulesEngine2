package service;

import io.swagger.sparks.ApiException;
import io.swagger.sparks.api.SiteAPIApi;
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

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparksService.class, SparksTokenRequest.class})
public class SparksServiceTest {
	@Autowired
	SparksService sparks;

	SiteAPIApi siteApi;


	@Before
	public void setup(){
		sparks.forceTokenRefresh();
		siteApi = sparks.getSiteApi();
	}

	@Test
	public void testsiteApi() throws ApiException {
		List<SiteAPIModel> subsites = siteApi.retrieveSubSites(155076L).getSites();
		System.out.println(subsites);
	}

}
