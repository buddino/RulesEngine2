package it.cnit.gaia.rulesengine.api;

import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.School;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

	@Autowired
	private RulesLoader rulesLoader;

	@RequestMapping(value = "/debug/trigger/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> triggerRuleOfSchool(@PathVariable Long id) throws Exception {
		School school = rulesLoader.getSchool(id);
		school.fire();
		return ResponseEntity.ok(null);
	}



}
