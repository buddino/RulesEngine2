import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.rules.ComfortIndex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestRule {

	@Autowired
	MeasurementRepository measurements;


	@Test
	public void test() {
		measurements.updateLatest();
		ComfortIndex rule = new ComfortIndex().setHumidUri("0013a2004091d30c/0xd17/hih4030").setTempUri("0013a2004091d30c/0xd17/lm35");
		rule.fire();
	}
}
