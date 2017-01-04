import it.cnit.gaia.rulesengine.model.GaiaRuleSet;
import model.RuleForTest;
import org.junit.Test;

public class TestModel {
	@Test
	public void testModel() {
		GaiaRuleSet ruleSetA = new GaiaRuleSet();
		for (int i = 0; i < 10; i++){
			ruleSetA.add(new RuleForTest().setName("A"+i));
		}
		GaiaRuleSet ruleSetB = new GaiaRuleSet();
		for (int i = 0; i < 10; i++){
			ruleSetB.add(new RuleForTest().setName("B"+i));
		}
		GaiaRuleSet container = new GaiaRuleSet();
		container.add(ruleSetA);
		container.add(ruleSetB);
		container.fire();

	}
}
