import it.cnit.gaia.rulesengine.model.Area;
import it.cnit.rulesengine.model.RuleForTest;
import org.junit.Test;

public class TestModel {
	@Test
	public void testModel() {
		Area ruleSetA = new Area();
		for (int i = 0; i < 10; i++){
			ruleSetA.add(new RuleForTest().setName("A"+i));
		}
		Area ruleSetB = new Area();
		for (int i = 0; i < 10; i++){
			ruleSetB.add(new RuleForTest().setName("B"+i));
		}
		Area container = new Area();
		container.add(ruleSetA);
		container.add(ruleSetB);
		container.fire();

	}
}
