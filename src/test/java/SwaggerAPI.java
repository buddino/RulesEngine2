import io.swagger.client.ApiException;
import io.swagger.client.model.SummaryDTO;
import it.cnit.gaia.rulesengine.measurements.SparksService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.OptionalDouble;
import java.util.stream.DoubleStream;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparksService.class})
public class SwaggerAPI {

	@Autowired
	SparksService sparksService;

	@Test
	public void testUriMapping(){
		try {
			Long aLong = sparksService.uri2id("gaia-prato/gw1/QG/pwf");
			System.out.println(aLong);
		} catch (ApiException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getSummary(){
		double minValue = 10.0;
		try {
			Date start = new Date();
			SummaryDTO summary = sparksService.getSummary(155389L);
			System.out.println("Query execution time: "+(new Date().getTime() - start.getTime()));
			DoubleStream doubleStream = summary.getDay().stream().filter(d -> d > minValue).mapToDouble(d -> d);
			OptionalDouble average = doubleStream.average();

			System.out.println(average);


		} catch (ApiException e) {
			e.printStackTrace();
		}
	}
}
