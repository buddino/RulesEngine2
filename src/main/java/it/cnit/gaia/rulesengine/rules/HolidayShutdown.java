package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import org.quartz.CronExpression;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.OptionalLong;

public class HolidayShutdown extends GaiaRule {
	@LoadMe
	public Long timeBeforeInHours;

	private List<CronExpression> cronexps = new ArrayList<>();

	@Override
	public boolean condition() {
		Long millisToNextTrigger = computeMillisToNextTrigger();
		if (millisToNextTrigger < 1000) {
			//You are inside a CRON PERIOD, don't trigger the rule
			return false;
		} else {
			Long hoursToNextTrigger = millisToNextTrigger / (1000 * 3600);    //Compute the time between the closest fireCron and now in hours
			return hoursToNextTrigger <= timeBeforeInHours;
		}
	}

	@Override
	public boolean init() throws RuleInitializationException {
		cronexps = metadataService.getClosed(155076L);
		validateFields();
		return true;
	}

	//Returns the closest timestamp in which the crons are verified
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
