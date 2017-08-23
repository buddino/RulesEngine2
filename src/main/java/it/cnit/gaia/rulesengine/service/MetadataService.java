package it.cnit.gaia.rulesengine.service;

import it.cnit.gaia.api.MetadataAPI;
import it.cnit.gaia.api.model.Schedule;
import it.cnit.gaia.api.model.Site;
import it.cnit.gaia.api.model.SiteInfo;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetadataService implements SparksAAAService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SparksTokenRequest tokenRequest;
	@Autowired
	private RulesLoader rulesLoader;
	@Autowired
	private MetadataAPI metadataAPI;

	private Map<Long, List<CronExpression>> occupation = new HashMap<>();
	private Map<Long, List<CronExpression>> schoolClosed = new HashMap<>();
	private Map<Long, List<CronExpression>> schoolTeaching = new HashMap<>();
	private Set<Long> areas = new HashSet<>();
	private Set<Long> buildings = new HashSet<>();

	@PostConstruct
	public void init() {
		tokenRequest.registerService(this);
	}

	public Collection<Site> getSites() {
		return metadataAPI.getSites().stream().map(x -> x.getSite()).collect(Collectors.toList());
	}

	public Collection<SiteInfo> getSiteInfos() {
		return metadataAPI.getSites().stream().map(x -> x.getSiteInfo()).collect(Collectors.toList());
	}

	public SiteInfo getSiteInfo(Long id) {
		return metadataAPI.getSiteInfo(id);
	}

	public Collection<Schedule> getSchedules(Long id) {
		return metadataAPI.getScheduleForSite(id);
	}

	public List<CronExpression> getClosed(Long id){
		return schoolClosed.get(id);
	}

	public List<CronExpression> getOccupied(Long id){
		return occupation.get(id);
	}

	public List<CronExpression> getTeaching(Long id){
		return schoolTeaching.get(id);
	}

	public void updateAll() {
		//Riguarda
		//Add schools
		Set<Long> schools = rulesLoader.loadSchools().keySet();
		this.buildings.addAll(schools);
		//Add areas
		Set<Long> areas = rulesLoader.getAreaMap().keySet();
		this.areas.addAll(areas);
		updateCalendars();
		updateSchedules();
	}

	public void updateSchedules() {
		for (Long aid : areas) {
			updateSchedule(aid);
		}
	}

	public void updateSchedule(Long aid) {
		Collection<Schedule> scheduleForSite = metadataAPI.getScheduleForSite(aid);
		List<String> cronStrings = scheduleForSite.stream().flatMap(l -> l.getCronDefinitions().stream())
												  .collect(Collectors.toList());
		List<CronExpression> cronExpressions = toExpressionList(cronStrings);
		occupation.put(aid, cronExpressions);
	}

	public void updateCalendars() {
		for (Long bid : buildings) {
			updateCalendar(bid);
		}
	}

	public void updateCalendar(Long bid) {
		Collection<Schedule> scheduleForSite = metadataAPI.getScheduleForSite(bid);
		List<Schedule> closed = scheduleForSite.stream().filter(s -> s.getType().equals("CLOSED"))
											   .collect(Collectors.toList());
		List<Schedule> teaching = scheduleForSite.stream().filter(s -> s.getType().equals("OPEN_ONGOING_ACTIVITIES"))
												 .collect(Collectors.toList());

		List<String> closedStrings = closed.stream().flatMap(l -> l.getCronDefinitions().stream())
										   .collect(Collectors.toList());
		List<String> teachingStrings = teaching.stream().flatMap(l -> l.getCronDefinitions().stream())
											   .collect(Collectors.toList());
		schoolClosed.put(bid, toExpressionList(closedStrings));
		schoolTeaching.put(bid, toExpressionList(teachingStrings));
	}

	public boolean isOccupied(Long id, Date date) throws BuildingDatabaseException {
		List<CronExpression> cronexpr = occupation.get(id);
		if (cronexpr == null) {
			updateSchedule(id);
			cronexpr = occupation.get(id);
			if (cronexpr == null)
				throw new BuildingDatabaseException("No schedule found for school " + id);
		}
		return cronexpr.stream().anyMatch(e -> e.isSatisfiedBy(date));
	}

	public boolean isClosed(Long sid, Date date) throws BuildingDatabaseException {
		List<CronExpression> cronexpr = schoolClosed.get(sid);
		if (cronexpr == null) {
			updateCalendar(sid);
			cronexpr = schoolClosed.get(sid);
			if (cronexpr == null)
				throw new BuildingDatabaseException("No schedule found for school " + sid);
		}
		return cronexpr.stream().anyMatch(e -> e.isSatisfiedBy(date));
	}

	public boolean isTeaching(Long aid, Date date) throws BuildingDatabaseException {
		List<CronExpression> cronexpr = schoolTeaching.get(aid);
		if (cronexpr == null) {
			updateCalendar(aid);
			cronexpr = schoolTeaching.get(aid);
			if (cronexpr == null)
				throw new BuildingDatabaseException("No schedule found for school " + aid);
		}
		return cronexpr.stream().anyMatch(e -> e.isSatisfiedBy(date));
	}

	public boolean isTeaching(Long id) throws BuildingDatabaseException {
		return isTeaching(id, new Date());
	}

	public boolean isClosed(Long id) throws BuildingDatabaseException {
		return isClosed(id, new Date());
	}

	public boolean isOccupied(Long id) throws BuildingDatabaseException {
		return isOccupied(id,new Date());
	}

	public void forceTokenRefresh() {
		tokenRequest.renewAccessToken();
	}

	public void setToken(String token) {
		metadataAPI.setToken(token);
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

}
