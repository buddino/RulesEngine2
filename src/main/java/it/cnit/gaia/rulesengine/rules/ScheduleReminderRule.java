package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.buildingdb.BuildingDatabaseException;
import it.cnit.gaia.buildingdb.ScheduleDTO;
import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
			Long hoursToNextTrigger = millisToNextTrigger / (1000 * 3600);	//Compute the time between the closest cron and now in hours
			return hoursToNextTrigger <= timeBeforeInHours;
		}
	}

	@Override
	public boolean init() {
		//Get area id
		areaId = getAreaId();
		try {
			//Get schedule
			ScheduleDTO scheduleForArea = buildingDBService.getScheduleForArea(areaId);
			for (String s : scheduleForArea.getCronstrs()) {
				//Create CRON expressions (and validate)
				cronexps.add(new CronExpression(s));
			}
		} catch (BuildingDatabaseException e) {
			LOGGER.error(e.getMessage());
			return false;
		} catch (ParseException e) {
			LOGGER.error("Cron expression not valid: " + e.getMessage());
			return false;
		}
		return validateFields();
	}

	//Returns the closest timestamp where the crons are verified
	private Long computeMillisToNextTrigger() {
		Date now = new Date();
		Long next = cronexps.stream()
				.mapToLong(x -> x.getNextValidTimeAfter(now)
						.getTime())
				.min()
				.getAsLong();
		return next - now.getTime();
	}

	public List<CronExpression> getCronexps() {
		return cronexps;
	}
}

