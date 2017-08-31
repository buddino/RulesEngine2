package it.cnit.gaia.rulesengine.api;

import com.orientechnologies.orient.core.record.impl.ODocument;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.cnit.gaia.rulesengine.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@ApiIgnore
@RequestMapping("stats")
public class StatsController {

	@Value("${scheduler.interval}")
	String schedulerInterval;

	@Autowired
	StatsService statsService;

	@GetMapping
	ResponseEntity<Map> getStats(){
		Map<String,Object> map = new HashMap<>();
		map.put("interval",schedulerInterval);
		return ResponseEntity.ok(map);
	}

	@ApiOperation(value = "GET events count", notes = "GET the number of events on the database in the specified time range, optinally filtered by rule class")
	@GetMapping("events/count")
	public ResponseEntity<Map> getEventsCounter(
			@ApiParam(value = "From time (timestamp)", example = "1492521504000") @RequestParam(required = false) Long from,
			@ApiParam(value = "To time (timestamp)", example = "1492511304000") @RequestParam(required = false) Long to,
			@ApiParam(value = "The rule classname", example = "ExploitNaturalLight") @RequestParam(required = false) String ruleClass) {
		Map<String, Long> map = new HashMap();
		if (ruleClass != null) {
			map.put("count", statsService.getEventsCount(from, to, ruleClass));
			return ResponseEntity.ok(map);
		}
		map.put("count", statsService.getEventsCount(from, to));
		return ResponseEntity.ok(map);
	}

	@ApiOperation(value = "GET events count grouped by class", notes = "GET the number of events on the database grouped by class")
	@GetMapping("events/classes")
	public ResponseEntity<Map> getEventsCounterForallClass(
			@ApiParam(value = "From time (timestamp)", example = "1492521504000") @RequestParam Long from,
			@ApiParam(value = "To time (timestamp)", example = "1492511304000") @RequestParam Long to) {
		Map<String, Long> map = new HashMap();
		List<ODocument> eventsForeachRuleClass = statsService.getEventsForeachRuleClass(from, to);
		for (ODocument o : eventsForeachRuleClass) {
			map.put(o.field("ruleClass"), o.field("count"));
		}
		return ResponseEntity.ok(map);
	}

	@ApiOperation(value = "GET events count", notes = "GET the number of events on the database in the specified time range, optinally filtered by rule class")
	@GetMapping("events/classes/latest")
	public ResponseEntity<Map> getEventsCounterForallClass() {
		Map<String, Long> map = new HashMap();
		LocalDateTime now = LocalDateTime.now();
		long from = now.minusDays(7).toInstant(ZoneOffset.UTC).toEpochMilli();
		List<ODocument> eventsForeachRuleClass = statsService
				.getEventsForeachRuleClass(from, now.toInstant(ZoneOffset.UTC).toEpochMilli());
		for (ODocument o : eventsForeachRuleClass) {
			map.put(o.field("ruleClass"), o.field("count"));
		}
		return ResponseEntity.ok(map);
	}

	@ApiOperation(value = "GET events count", notes = "GET the number of events on the database grouped by day for the latest N days, passed as parameter. Default 7 days.")
	@GetMapping("events/latest")
	public ResponseEntity<Map> getLastDaysEvents(
			@ApiParam(value = "Number of days to look back", example = "10") @RequestParam(required = false, defaultValue = "7") Long days,
			@ApiParam(value = "The rule classname", example = "ExploitNaturalLight") @RequestParam(required = false) String ruleClass) {
		//Linked for maintaining insertion order
		Map<String, Long> map = new LinkedHashMap();
		List<ODocument> countLatestNDaysForClass;
		if (ruleClass != null) {
			countLatestNDaysForClass = statsService.getCountLatestNDaysForClass(days, ruleClass);
		} else {
			countLatestNDaysForClass = statsService.getCountLatestNDays(days);

		}
		for (ODocument o : countLatestNDaysForClass) {
			map.put(o.field("ts"), o.field("events"));
		}
		return ResponseEntity.ok(map);
	}

	@ApiOperation(value = "GET rule instances count", notes = "GET the number of the rule instances.")
	@GetMapping("rules")
	public ResponseEntity<Map> getRuleInstancesCount() {
		//Linked for maintaining insertion order
		Map<String, Object> map = new LinkedHashMap();
		Long active = statsService.getCountActiveRuleInstances();
		Long stored = statsService.getCountRuleInstanceInDb();
		Long disabled = statsService.getDisabledRules();
		Long custom = statsService.getCustomRules();
		map.put("active", active);
		map.put("stored", stored);
		map.put("custom", custom);
		map.put("disabled", disabled);
		List<ODocument> rulesGroupedByClass = statsService.getRulesGroupedByClass();
		Map<String, Long> group = new LinkedHashMap();
		for( ODocument o : rulesGroupedByClass ){
			group.put(o.field("class"), o.field("count"));
		}
		map.put("rules",group);
		return ResponseEntity.ok(map);
	}

	@ApiOperation(value = "GET rule instances count", notes = "GET the number of the rule instances.")
	@GetMapping("rules/latest")
	public ResponseEntity<List<Map>> getLatestRules() {
		//Linked for maintaining insertion order
		List<Map> list = new ArrayList<>();
		List<ODocument> latestFiredRules = statsService.getLatestFiredRules();
		for (ODocument o : latestFiredRules) {
			Map<String, Long> map = new LinkedHashMap();
			map.put("name", o.field("name"));
			map.put("class", o.field("class"));
			map.put("school", o.field("school"));
			map.put("latest", o.field("latestFireTime"));
			list.add(map);
		}
		return ResponseEntity.ok(list);
	}


}
