package it.cnit.gaia.rulesengine.api;

import it.cnit.gaia.rulesengine.loader.RulesLoader;
import it.cnit.gaia.rulesengine.model.School;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

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

	@RequestMapping(value = "/debug/log", method = RequestMethod.GET, produces= MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> getLog() throws FileNotFoundException {
		FileReader fr = new FileReader("./RulesEngine.log");
		BufferedReader  br = new BufferedReader(fr);
		StringBuffer bf = new StringBuffer();
		br.lines().forEach(x->bf.append(x).append("\r\n"));
		return ResponseEntity.ok(bf.toString());
	}



}
