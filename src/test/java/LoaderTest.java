import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.cnit.gaia.rulesengine.configuration.OrientConfiguration;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.measurements.SwaggerClient;
import it.cnit.gaia.rulesengine.model.Fireable;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.GaiaRuleSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RulesLoader.class, OrientConfiguration.class, MeasurementRepository.class, SwaggerClient.class})
public class LoaderTest {

	@Autowired
	RulesLoader rulesLoader;

	@Test
	public void test(){
		Fireable root = rulesLoader.getRuleTree("#27:0");
		JsonElement traverse = traverse(root);
		System.out.println(traverse);
	}

	public JsonElement traverse(Fireable root){
		if( root instanceof GaiaRuleSet){
			JsonArray arr = new JsonArray();
			Set<Fireable> ruleSet = ((GaiaRuleSet) root).getRuleSet();
			for(Fireable f : ruleSet){
				arr.add(traverse(f));
			}
			return arr;
		}
		else {
			JsonObject obj = new JsonObject();
			obj.addProperty("rule",((GaiaRule) root).getName());
			return obj;
		}
	}
}
