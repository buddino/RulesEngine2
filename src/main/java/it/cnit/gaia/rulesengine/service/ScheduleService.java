package it.cnit.gaia.rulesengine.service;

import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.buildingdb.dto.AreaScheduleDTO;
import it.cnit.gaia.buildingdb.dto.BuildingCalendarDTO;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import org.quartz.CronExpression;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

@Service
public class ScheduleService {

	protected final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	BuildingDatabaseService bds;

	//@Autowired
	//RulesLoader rulesLoader;

	private Map<Long, List<CronExpression>> occupation = new HashMap<>();
	private Map<Long, List<CronExpression>> vacation = new HashMap<>();
	private Set<Long> areas = new HashSet<>();
	private Set<Long> buildings = new HashSet<>();

	/**
	 * Retrieve the schedule for an area
	 * This should be done once during tree loading
	 * Parse the cron expressions
	 * <p>
	 * Provide methods for:
	 * - verify if a Date ( usually now() ) is verified by any of the cron expression for the specific area
	 * - ...
	 */

	//TODO Schedule may be update once a day or by forced update
	public ScheduleService() {
		areas.add(47L);
		areas.add(48L);
		areas.add(27L);
	}

	public void updateSchedules() throws ParseException, BuildingDatabaseException, IOException {
		for (Long aid : areas) {
			occupation.put(aid, getCronExpressionsForArea(aid));
		}
	}

	public void updateCalendars() throws ParseException, BuildingDatabaseException, IOException {
		for (Long sid : buildings) {
			vacation.put(sid, getCalendarForBuilding(sid));
		}
	}

	/**
	 * Returns true in the area identified by aid is occupied in the specified date
	 *
	 * @param aid  Area identifier
	 * @param date Date to be verified
	 * @return true if date is in one of the interval specified
	 * @throws BuildingDatabaseException
	 * @throws ParseException
	 */
	public boolean isOccuipied(Long aid, Date date) throws ParseException, BuildingDatabaseException, IOException {
		List<CronExpression> cronexpr = occupation.get(aid);
		if (cronexpr != null)
			return cronexpr.stream().anyMatch(e -> e.isSatisfiedBy(date));
		else
			//TODO Throw exception, no cron expression in memory
			return false;
	}

	/**
	 * Returns true in the area identified by aid is occupied in this moment
	 *
	 * @param aid Area identifier
	 * @return true if the area is occupied in this moment
	 * @throws BuildingDatabaseException
	 * @throws ParseException
	 */
	public boolean isOccuipied(Long aid) throws BuildingDatabaseException, ParseException, IOException {
		return isOccuipied(aid, new Date());
	}

	/**
	 * Returns true in the area identified by aid is closed in the specified date
	 *
	 * @param aid  Area identifier
	 * @param date Date to be verified
	 * @return true id in date the area is closed
	 * @throws BuildingDatabaseException
	 * @throws ParseException
	 */
	public boolean isClosed(Long aid, Date date) throws BuildingDatabaseException, ParseException {
		//ScheduleDTO scheduleForArea = bds.getScheduleForArea(aid);
		//Collection<String> cronstrs = scheduleForArea.getCronstrs();
		//List<CronExpression> cronexpr = toExpressionList(cronstrs);
		List<CronExpression> cronexpr = vacation.get(aid);
		return cronexpr.stream().anyMatch(e -> e.isSatisfiedBy(date));
	}

	/**
	 * Returns true in the area identified by aid is occupied in this moment
	 *
	 * @param aid Area identifier
	 * @return true if the area is closed in this moment
	 * @throws BuildingDatabaseException
	 * @throws ParseException
	 */
	public boolean isClosed(Long aid) throws BuildingDatabaseException, ParseException {
		return isClosed(aid, new Date());
	}


	private List<CronExpression> getCalendarForBuilding(Long sid) throws BuildingDatabaseException, IOException, ParseException {
		List<BuildingCalendarDTO> buildingCalendar = bds.getBuildingCalendar(sid);
		List<CronExpression> cronExpressions = new ArrayList<>();
		for (BuildingCalendarDTO calendar : buildingCalendar) {
			List<CronExpression> partialcronexp = toExpressionList(calendar.getCronStrings());
			cronExpressions.addAll(partialcronexp);
		}
		return cronExpressions;
	}

	private List<CronExpression> getCronExpressionsForArea(Long aid) throws BuildingDatabaseException, IOException, ParseException {
		List<AreaScheduleDTO> schedulesForArea = bds.getScheduleForArea(aid);
		List<CronExpression> cronExpressions = new ArrayList<>();
		for (AreaScheduleDTO schedule : schedulesForArea) {
			List<CronExpression> partialcronexp = toExpressionList(schedule.getCronStrings());
			cronExpressions.addAll(partialcronexp);
		}
		return cronExpressions;
	}

	private List<CronExpression> toExpressionList(Collection<String> strings) {
		List<CronExpression> cronexpr = new LinkedList<>();
		for (String s : strings) {
			try {
				CronExpression e = new CronExpression(s);
				cronexpr.add(e);
			} catch (ParseException e) {
				LOGGER.warn("Could not parse: "+s);
			}
		}
		return cronexpr;
	}

	//TODO How long before the closest valid moment expressed by the CRON Expressions

}
