package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.buildingdb.dto.AreaScheduleDTO;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import org.quartz.CronExpression;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.OptionalLong;

public class ScheduleReminderRule extends GaiaRule {
	@LoadMe
	public Long timeBeforeInHours;

	@LogMe
	Long areaId;

	private List<CronExpression> cronexps = new ArrayList<>();

	@Override
	public boolean condition() {
		Long millisToNextTrigger = computeMillisToNextTrigger();
		if (millisToNextTrigger < 1000) {
			//You are inside a CRON PERIOD, don't trigger the rule
			return false;
		} else {
			Long hoursToNextTrigger = millisToNextTrigger / (1000 * 3600);    //Compute the time between the closest cron and now in hours
			return hoursToNextTrigger <= timeBeforeInHours;
		}
	}

	@Override
	public boolean init() throws RuleInitializationException {
		//Get area id
		areaId = ruleDatabaseService.getParentArea(rid);
		try {
			//Get schedule
			List<AreaScheduleDTO> schedulesForArea = buildingDBService.getScheduleForArea(areaId);
			if (schedulesForArea == null)
				throw new RuleInitializationException("Error retrieving the schedule for area " + areaId);
			for( AreaScheduleDTO areaScheduleDTO : schedulesForArea ){
				if (areaScheduleDTO == null)
					throw new RuleInitializationException("Error retrieving the schedule for area " + areaId);
				List<String> cronStrings = areaScheduleDTO.getCronStrings();
				if( cronStrings.size() == 0)
					throw new RuleInitializationException("The CRON expressions collection is empty");
				for (String s : cronStrings) {
					//Create CRON expressions (and validate)
					cronexps.add(new CronExpression(s));
				}
			}
		} catch (BuildingDatabaseException e) {
			LOGGER.error(e.getMessage());
			return false;
		} catch (ParseException e) {
			throw new RuleInitializationException("Cron expression not valid: " + e.getMessage());
		} catch (IOException e) {
			throw new RuleInitializationException("JSON Array parsing error: " + e.getMessage());
		}
		validateFields();
		return true;
	}

	//Returns the closest timestamp where the crons are verified
	private Long computeMillisToNextTrigger() {
		Date now = new Date();
		OptionalLong next;
		try {
			next = cronexps.stream().mapToLong(x -> x.getNextValidTimeAfter(now).getTime())
						   .min();
		} catch (NullPointerException e) {
			return 0L;
		}
		if (next.isPresent())
			return next.getAsLong() - now.getTime();
		else
			return 0L;
	}

	public List<CronExpression> getCronexps() {
		return cronexps;
	}
}
