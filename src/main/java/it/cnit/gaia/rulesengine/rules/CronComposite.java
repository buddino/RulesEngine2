package it.cnit.gaia.rulesengine.rules;


import it.cnit.gaia.rulesengine.model.GaiaRule;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CronComposite extends CompositeRule{
	public GaiaRule rule;
	private List<CronExpression> cronexps = new ArrayList<>();
	public List<String> cronstrs = new ArrayList<>();

	//Da lista di string a espressione e validazione
	//return a.compareTo(d) * d.compareTo(b) >= 0;
	//return d.after(min) && d.before(max);

	//TODO Add fields

	@Override
	public boolean condition() {
		Date now = new Date();
		//If at least one of the cron is satisfied
		if(cronexps.stream().anyMatch(ce -> ce.isSatisfiedBy(now))){

		}

		//If at least one of the cron is satisfied
		if(cronexps.stream().allMatch(ce -> ce.isSatisfiedBy(now))){

		}
		return false;
	}

	@Override
	public boolean init(){
		boolean result = true;
		if(ruleSet.size()==1) {
			rule = ruleSet.iterator().next();
			for(String s : cronstrs){
				try {
					cronexps.add(new CronExpression(s));
				} catch (ParseException e) {
					LOGGER.error("Cron expression '"+s+"' is not valid");
					result=false;
				}
			}
		}
		else  {
			result = false;
			if(ruleSet.size()==0)
				LOGGER.error("No rule linked");
			else
				LOGGER.error("More than ONE rule linked");
		}
		return result;
	}
}
