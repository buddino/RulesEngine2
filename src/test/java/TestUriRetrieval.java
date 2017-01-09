import io.swagger.client.ApiException;
import io.swagger.client.model.ResourceDTO;
import it.cnit.gaia.rulesengine.measurements.MeterMap;
import it.cnit.gaia.rulesengine.measurements.SwaggerClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SwaggerClient.class, MeterMap.class})
public class TestUriRetrieval {

	@Autowired
	SwaggerClient sparks;

	@Test
	public void testUriRetrieval(){
		try {
			ResourceDTO dto = sparks.resApi.getByUriUsingGET("gaia-prato/gw1/QG/Lighting/actpw");
			System.out.println(dto);
		} catch (ApiException e) {
			e.printStackTrace();
		}
	}
}
