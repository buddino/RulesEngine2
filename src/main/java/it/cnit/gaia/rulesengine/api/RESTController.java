package it.cnit.gaia.rulesengine.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.measurements.MeasurementRepository;
import it.cnit.gaia.rulesengine.model.Fireable;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.GaiaRuleSet;
import it.cnit.gaia.rulesengine.rules.CompositeRule;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class RESTController {
	Logger LOGGER = Logger.getRootLogger();

	@Autowired
	RulesLoader rulesLoader;
	@Autowired
	MeasurementRepository measurementRepository;

	Gson g = new Gson();

	@RequestMapping("/uris")
	public String getUris() {
		return g.toJson(measurementRepository.getUriSet()).toString();
	}

	@RequestMapping("/rules/update")
	public void updateRules() {
		LOGGER.info("Updateing rules tree in next iteration");
		rulesLoader.updateRuleTree();
	}

	@RequestMapping("/rules/{rid}")
	public String getrulestree(@PathVariable(value = "rid") String rid) {
		System.out.println(rid);
		Fireable f = rulesLoader.getRuleTree("#" + rid);
		JsonElement j = traverse(f);
		return j.toString();
	}

	@RequestMapping("/rules")
	public String getrulestree() {
		Fireable f = rulesLoader.getRoot();
		if (f == null)
			return "{}";
		JsonElement j = traverse(f);
		return j.toString();
	}

	@RequestMapping("/urimapping")
	public String getUriMapping() {
		return g.toJson(measurementRepository.getMeterMap());
	}



	public JsonElement traverse(Fireable root) {
		//Riguarda composite e ruleset forse ne basta uno solo
		if (root instanceof GaiaRuleSet) {
			JsonObject obj = new JsonObject();
			obj.addProperty("rule", root.getClass().getSimpleName());
			obj.addProperty("rid", ((GaiaRuleSet) root).getRid());
			JsonArray arr = new JsonArray();
			Set<Fireable> ruleSet = ((GaiaRuleSet) root).getRuleSet();
			for (Fireable f : ruleSet) {
				arr.add(traverse(f));
			}
			obj.add("rules", arr);
			return obj;
		} else if (root instanceof CompositeRule) {
			JsonObject obj = new JsonObject();
			obj.addProperty("rule", root.getClass().getSimpleName());
			obj.addProperty("rid", ((CompositeRule) root).getRid());
			JsonArray arr = new JsonArray();
			Set<GaiaRule> ruleSet = ((CompositeRule) root).getRuleSet();
			for (Fireable f : ruleSet) {
				arr.add(traverse(f));
			}
			obj.add("rules", arr);
			return obj;
		} else {
			JsonObject obj = new JsonObject();
			obj.addProperty("rule", ((GaiaRule) root).getClass().getSimpleName());
			obj.addProperty("rid", ((GaiaRule) root).getRid());
			return obj;
		}
	}


}
