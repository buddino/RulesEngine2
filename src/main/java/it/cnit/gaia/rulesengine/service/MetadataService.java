package it.cnit.gaia.rulesengine.service;

import it.cnit.gaia.buildingdb.BuildingDatabaseService;
import it.cnit.gaia.buildingdb.dto.*;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.quartz.CronExpression;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * Provides the access to the metadata for the buildings / areas
 * including: Schedules / Calendars / Additional fields in the json
 */
@Service
public class MetadataService {

	protected final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(this.getClass());



	@Autowired
	BuildingDatabaseService bds;
	@Autowired
	RulesLoader rulesLoader;
	private ObjectMapper mapper = new ObjectMapper();

	//@Autowired
	//RulesLoader rulesLoader;
	private Map<Long, List<CronExpression>> occupation = new HashMap<>();
	private Map<Long, List<CronExpression>> schoolClosed = new HashMap<>();
	private Map<Long, List<CronExpression>> schoolTeaching = new HashMap<>();
	private Set<Long> areas = new HashSet<>();
	private Set<Long> buildings = new HashSet<>();

	/**
	 * Retrieve the schedule for an area
	 * This should be done once during tree loading
	 * Parse the fireCron expressions
	 * <p>
	 * Provide methods for:
	 * - verify if a Date ( usually now() ) is verified by any of the fireCron expression for the specific area
	 * - ...
	 */
	public void updateAll() {
		//Riguarda
		//Add schools
		Set<Long> schools = rulesLoader.loadSchools().keySet();
		buildings.addAll(schools);
		//Add areas
		Set<Long> areas = rulesLoader.getAreaMap().keySet();
		this.areas.addAll(areas);
		try {
			updateCalendars();
			updateSchedules();
		} catch (ParseException e) {
			LOGGER.warn("Parsing error: " + e.getMessage());
		} catch (BuildingDatabaseException e) {
			LOGGER.warn("Building database exception: " + e.getMessage());
		} catch (HttpMessageNotReadableException e) {
			LOGGER.warn("Message not readable: " + e.getMessage());
		}
	}

	public void updateSchedule(Long aid) throws BuildingDatabaseException, ParseException {
		LOGGER.debug("Updating schedule for area: " + aid);
		try {
			occupation.put(aid, getCronExpressionsForArea(aid));
		} catch (IOException e) {
			LOGGER.warn("Error while loading schedule for area " + aid);
		}
	}

	public void updateCalendar(Long sid) throws BuildingDatabaseException, ParseException {
		LOGGER.debug("Updating schedule for area: " + sid);
		try {
			occupation.put(sid, getCronExpressionsForArea(sid));
		} catch (IOException e) {
			LOGGER.warn("Error while loading schedule for area " + sid);
		}
	}

	public void updateSchedules() throws ParseException, BuildingDatabaseException {
		for (Long aid : areas) {
			LOGGER.debug("Updating schedule for area: " + aid);
			try {
				occupation.put(aid, getCronExpressionsForArea(aid));
			} catch (IOException e) {
				LOGGER.warn("Error while loading schedule for area " + aid);
			}
		}
	}

	public void updateCalendars() throws ParseException, BuildingDatabaseException {
		for (Long sid : buildings) {
			LOGGER.debug("Updating calendar for building: " + sid);
			try {
				List<CronExpression> calendarClosed = getCalendarForBuilding(sid, CalendarStatus.CLOSED);
				List<CronExpression> calendarTeaching = getCalendarForBuilding(sid, CalendarStatus.TEACHING);
				schoolClosed.put(sid, calendarClosed);
				schoolTeaching.put(sid, calendarTeaching);
			} catch (IOException e) {
				LOGGER.warn("Error while loading calendar for school " + sid);
			}
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
	public boolean isOccupied(Long aid, Date date) {
		List<CronExpression> cronexpr = occupation.get(aid);
		if (cronexpr != null)
			return cronexpr.stream().anyMatch(e -> e.isSatisfiedBy(date));
		else
			//TODO Throw exception, no fireCron expression in memory
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
	public boolean isOccupied(Long aid) {
		return isOccupied(aid, new Date());
	}

	/**
	 * Returns true in the area identified by aid is closed in the specified date
	 *
	 * @param sid  Building identifier
	 * @param date Date to be verified
	 * @return true id in date the area is closed
	 * @throws BuildingDatabaseException
	 * @throws ParseException
	 */
	public boolean isClosed(Long sid, Date date) throws BuildingDatabaseException {
		List<CronExpression> cronexpr = schoolClosed.get(sid);
		if (cronexpr == null) {
			try {
				updateCalendar(sid);
			} catch (BuildingDatabaseException e) {
				LOGGER.warn(e.getMessage());
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage());
			}
			cronexpr = schoolClosed.get(sid);
			if (cronexpr == null)
				throw new BuildingDatabaseException("No schedule found for school " + sid);
		}
		return cronexpr.stream().anyMatch(e -> e.isSatisfiedBy(date));
	}

	/**
	 * Returns true in the area identified by aid is occupied in this moment
	 *
	 * @param sid Building identifier
	 * @return true if the area is closed in this moment
	 * @throws BuildingDatabaseException
	 * @throws ParseException
	 */
	public boolean isClosed(Long sid) throws BuildingDatabaseException {
		return isClosed(sid, new Date());
	}

	public boolean isTeaching(Long sid, Date date) throws BuildingDatabaseException {
		List<CronExpression> cronexpr = schoolTeaching.get(sid);
		//FIXME Vengono richieste aree
		if (cronexpr == null) {
			try {
				updateSchedule(sid);
			} catch (ParseException e) {
				LOGGER.warn(e.getMessage());
			}
			cronexpr = schoolTeaching.get(sid);
			if (cronexpr == null)
				throw new BuildingDatabaseException("No schedule found for school " + sid);
		}
		return cronexpr.stream().anyMatch(e -> e.isSatisfiedBy(date));
	}

	public boolean isTeaching(Long sid) throws BuildingDatabaseException {
		return isTeaching(sid, new Date());
	}


	private List<CronExpression> getCalendarForBuilding(Long sid, CalendarStatus type) throws BuildingDatabaseException, IOException, ParseException {
		List<BuildingCalendarDTO> buildingCalendar = bds.getBuildingCalendar(sid, type);
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
				LOGGER.warn("Could not parse: " + s);
			}
		}
		return cronexpr;
	}

	public JsonNode getJsonFieldForArea(Long aid, String field) throws BuildingDatabaseException, IOException {
		AreaDTO area = bds.getAreaById(aid);
		String json = area.getJson().toString();
		return mapper.readTree(json).get(field);
	}

	public JsonNode getJsonFieldForbuilding(Long sid, String field) throws BuildingDatabaseException, IOException {
		BuildingDTO building = bds.getBuildingById(sid);
		String json = building.getJson().toString();
		return mapper.readTree(json).get(field);
	}
}
