package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.intervalparser.LocalInterval;
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

	private List<LocalInterval> intervals = new ArrayList<>();
	private List<CronExpression> cronexps = new ArrayList<>();

	@Override
	public boolean condition() {
		Long millisToNextTrigger = computeMillisToNextTrigger();
		if (millisToNextTrigger < 0) {
			//You are inside the interval, DO NOT trigger
			return false;
		} else {
			Long hoursToNextTrigger = millisToNextTrigger / (1000 * 3600);    //Compute the time between the closest fireCron and now in hours
			return hoursToNextTrigger <= timeBeforeInHours;
		}
	}

	@Override
	public boolean init() throws RuleInitializationException {
		//FIXME Will return alist of interval instead of cronexp
		//intervals = metadataService.getClosed(155076L);
		cronexps = metadataService.getClosed(155076L);
		validateFields();
		return true;
	}

	//Returns the closest timestamp in which the crons are verified
	private Long computeMillisToNextTrigger() {
		Date now = new Date();
		OptionalLong next;
		try {
			next = intervals.stream().mapToLong(x -> x.getStartAsLong()).min();
		} catch (NullPointerException e) {
			return Long.MAX_VALUE;
		}
		if (next.isPresent())
			return next.getAsLong() - now.getTime();
		else
			return Long.MAX_VALUE;
	}

	public List<CronExpression> getCronexps() {
		return cronexps;
	}
}
