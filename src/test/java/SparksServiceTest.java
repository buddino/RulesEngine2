import io.swagger.client.ApiException;
import io.swagger.client.model.ResourceDataDTO;
import io.swagger.client.model.SummaryDTO;
import it.cnit.gaia.rulesengine.service.SparksService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparksService.class})
public class SparksServiceTest {

	@Autowired
	SparksService sparksService;

	@Test
	public void testUriMapping(){
		try {
			long resourceId = sparksService.uri2id("gaia-prato/gw1/QG/pwf");
			Assert.assertEquals(resourceId, 155389L);
		} catch (ApiException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getSummary(){
		try {
			Date start = new Date();
			SummaryDTO summary = sparksService.getSummary(155389L);
			Assert.assertEquals(summary.getKeyName(),"gaia-prato/gw1/QG/pwf");

		} catch (ApiException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getLatest(){
		Map<String, Long> meterMap = new HashMap<>();
		meterMap.put("gaia-prato/gw1/QG/pwf",155389L);
		sparksService.setMeterMap(meterMap);
		try {
			Map<String, ResourceDataDTO> dataDTOMap = sparksService.queryLatest();
			ResourceDataDTO resourceDataDTO = dataDTOMap.get("gaia-prato/gw1/QG/pwf");
			Assert.assertTrue(resourceDataDTO.getReading()>=0.0);
		} catch (ApiException e) {
			e.printStackTrace();
		}
	}


}
