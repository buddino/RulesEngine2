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
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.model.errors.ResourceNotFoundException;
import it.cnit.gaia.rulesengine.rules.CompositeRule;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@RestController
public class RESTController {
	private Logger LOGGER = Logger.getRootLogger();
	private Gson g = new Gson();

	@Autowired
	private RulesLoader rulesLoader;

	@Autowired
	private MeasurementRepository measurementRepository;

	@RequestMapping("/rules/{schoolId}")
	public String getrulestree(@PathVariable String schoolId) {
		School school = rulesLoader.loadSchools().get(schoolId);
		if (school == null)
			throw new ResourceNotFoundException();
		Fireable root = school.getRoot();
		JsonElement json = traverse(root);
		return json.toString();
	}

	@RequestMapping("/schools")
	public
	@ResponseBody
	Collection<School> getSchools() {
		return rulesLoader.loadSchools().values();
	}

	@ResponseBody
	@RequestMapping("/reload/{schoolId}")
	public ResponseEntity<String> reload(@PathVariable(required = false) String schoolId) {
		if(schoolId==null || schoolId.equals("")) {
			rulesLoader.reloadAllSchools();
			return new ResponseEntity<>(HttpStatus.OK);
		} else {
			if(!rulesLoader.reloadSchool(schoolId)){
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	@RequestMapping("/uris")
	public
	@ResponseBody
	Map<String, Long> getUriMapping() {
		return measurementRepository.getMeterMap();
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
