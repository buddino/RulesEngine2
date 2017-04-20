package it.cnit.gaia.rulesengine.service;

import it.cnit.gaia.buildingdb.BuildingDatabaseException;
import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.buildingdb.ScheduleDTO;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Service
public class ScheduleService {

	@Autowired
	BuildingDatabaseService bds;

	Map<Long,List<CronExpression>> cronMap;
	Set<Long> areas;
	/**
	 * Retrieve the schedule for an area
	 * This should be done once during tree loading
	 * Parse the cron expressions
	 *
	 * Provide methods for:
	 *  - verify if a Date ( usually now() ) is verified by any of the cron expression for the specific area
	 *  -
	 */

	public ScheduleService(){
	}

	public void updateSchedules(){
		//TODO Build the map Area --> List<CronExpressions>
	}

	public boolean isInInterval(Long aid, Date date) throws BuildingDatabaseException, ParseException {
		ScheduleDTO scheduleForArea = bds.getScheduleForArea(aid);
		Collection<String> cronstrs = scheduleForArea.getCronstrs();
		List<CronExpression> cronexpr = toExpressionList(cronstrs);
		//List<CronExpression> cronexpr = cronMap.get(aid);
		return cronexpr.stream().anyMatch(e -> e.isSatisfiedBy(date));
	}

	public boolean isInInterval(Long aid) throws BuildingDatabaseException, ParseException {
		return isInInterval(aid, new Date());
	}

	private List<CronExpression> toExpressionList(Collection<String> strings) throws ParseException {
		List<CronExpression> cronexpr = new LinkedList<>();
		for(String s : strings){
			CronExpression e = new CronExpression(s);
			cronexpr.add(e);
		}
		return cronexpr;
	}

	//TODO How long before the closest valid moment expressed by the CRON Expressions

}
