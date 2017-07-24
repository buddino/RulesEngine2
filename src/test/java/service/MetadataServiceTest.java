package service;

import io.swagger.metadata.ApiException;
import io.swagger.metadata.model.ResourceSiteInfo;
import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import it.cnit.gaia.rulesengine.service.MetadataService2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MetadataService2.class, SparksTokenRequest.class})
public class MetadataServiceTest {

	@Autowired
	MetadataService2 metadataService;

	@Test
	public void test() throws ApiException {
		metadataService.forceTokenRefresh();
		ResourceSiteInfo oneSiteInfoUsingGET = metadataService.getSiteInfoEntityApi().findOneSiteInfoUsingGET(155076L);
		System.out.println(oneSiteInfoUsingGET);
	}


}
