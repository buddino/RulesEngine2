package it.cnit.gaia.rulesengine.rules;


import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CronComposite extends CompositeRule {
	public GaiaRule rule;
	private List<CronExpression> cronexps = new ArrayList<>();

	@LoadMe
	public List<String> cronstrs = new ArrayList<>();
	@LogMe
	@LoadMe(required = false)
	public boolean negative = false;

	//Da lista di string a espressione e validazione
	//return a.compareTo(d) * d.compareTo(b) >= 0;
	//return d.after(min) && d.before(max);

	//TODO Add fields

	@Override
	public boolean condition() {
		Date now = new Date();

		if (negative) {
			//If all the cron are not satisfied
			return cronexps.stream().allMatch(ce -> !ce.isSatisfiedBy(now));
		} else {
			//If at least one of the cron is satisfied
			return cronexps.stream().anyMatch(ce -> ce.isSatisfiedBy(now));
		}
	}

	@Override
	public void action(){
		rule.fire();
	}

	@Override
	public boolean init() throws RuleInitializationException {
		boolean result = true;
		if (ruleSet.size() == 1) {
			rule = ruleSet.iterator().next();
			for (String s : cronstrs) {
				try {
					cronexps.add(new CronExpression(s));
				} catch (ParseException e) {
					throw new RuleInitializationException("Cron expression '" + s + "' is not valid");
				}
			}
		} else {
			if (ruleSet.size() == 0)
				throw new RuleInitializationException("No linked rules");
			else
				LOGGER.error("More than ONE rule linked");
		}
		return true;
	}
}
