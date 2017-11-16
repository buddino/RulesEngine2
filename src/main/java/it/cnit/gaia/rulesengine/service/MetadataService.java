package it.cnit.gaia.rulesengine.service;

import it.cnit.gaia.api.MetadataAPI;
import it.cnit.gaia.api.model.Schedule;
import it.cnit.gaia.api.model.Site;
import it.cnit.gaia.api.model.SiteInfo;
import it.cnit.gaia.buildingdb.exceptions.BuildingDatabaseException;
import it.cnit.gaia.intervalparser.LocalInterval;
import it.cnit.gaia.intervalparser.LocalIntervalParser;
import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import it.cnit.gaia.rulesengine.loader.RulesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetadataService implements SparksAAAService {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());


	@Autowired
	private LocalIntervalParser intervalParser;
	@Autowired
	private SparksTokenRequest tokenRequest;
	@Autowired
	private RulesLoader rulesLoader;
	@Autowired
	private MetadataAPI metadataAPI;


	private Map<Long, List<LocalInterval>> occupation = new HashMap<>();
	private Map<Long, List<LocalInterval>> schoolClosed = new HashMap<>();
	private Map<Long, List<LocalInterval>> schoolTeaching = new HashMap<>();
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

	public List<LocalInterval> getClosed(Long id) {
		return schoolClosed.get(id);
	}

	public List<LocalInterval> getOccupied(Long id) {
		return occupation.get(id);
	}

	public List<LocalInterval> getTeaching(Long id) {
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
		areas.parallelStream().forEach(aid -> updateSchedule(aid));
	}

	public void updateSchedule(Long aid) {
		try {
			Collection<Schedule> scheduleForSite = metadataAPI.getScheduleForSite(aid);
			List<String> cronStrings = scheduleForSite.stream().flatMap(l -> l.getCronDefinitions().stream())
													  .collect(Collectors.toList());
			List<LocalInterval> intervalList = toIntervalList(cronStrings);
			occupation.put(aid, intervalList);
		}
		catch (HttpStatusCodeException e){
			LOGGER.warn("Error while updating schedule for: "+aid+". Error: "+e.getStatusText());
		}
	}

	public void updateCalendars() {
		buildings.parallelStream().forEach(bid -> updateCalendar(bid));
	}

	public void updateCalendar(Long bid) {
		try {
			Collection<Schedule> scheduleForSite = metadataAPI.getScheduleForSite(bid);
			List<Schedule> closed = scheduleForSite.stream().filter(s -> s.getType().equals("CLOSED"))
												   .collect(Collectors.toList());
			List<Schedule> teaching = scheduleForSite.stream()
													 .filter(s -> s.getType().equals("OPEN_ONGOING_ACTIVITIES"))
													 .collect(Collectors.toList());

			List<String> closedStrings = closed.stream().flatMap(l -> l.getCronDefinitions().stream())
											   .collect(Collectors.toList());
			List<String> teachingStrings = teaching.stream().flatMap(l -> l.getCronDefinitions().stream())
												   .collect(Collectors.toList());
			schoolClosed.put(bid, toIntervalList(closedStrings));
			schoolTeaching.put(bid, toIntervalList(teachingStrings));
		}
		catch (HttpStatusCodeException e){
			LOGGER.warn("Error while updating schedule for: "+bid+". Error: "+e.getStatusText());
		}
	}

	public boolean isOccupied(Long id, Date date) throws BuildingDatabaseException {
		LocalDateTime queryDate = LocalDateTime
				.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
		List<LocalInterval> intervals = occupation.get(id);
		if (intervals == null) {
			updateSchedule(id);
			intervals = occupation.get(id);
			if (intervals == null)
				throw new BuildingDatabaseException("No schedule found for school " + id);
		}
		return intervals.stream().anyMatch(e -> e.inRange(queryDate));
	}

	public boolean isClosed(Long sid, Date date) throws BuildingDatabaseException {
		LocalDateTime queryDate = LocalDateTime
				.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
		List<LocalInterval> intervals = schoolClosed.get(sid);
		if (intervals == null) {
			updateCalendar(sid);
			intervals = schoolClosed.get(sid);
			if (intervals == null)
				throw new BuildingDatabaseException("No schedule found for school " + sid);
		}
		return intervals.stream().anyMatch(e -> e.inRange(queryDate));
	}

	public boolean isTeaching(Long aid, Date date) throws BuildingDatabaseException {
		LocalDateTime queryDate = LocalDateTime
				.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
		List<LocalInterval> intervals = schoolClosed.get(aid);
		if (intervals == null) {
			updateCalendar(aid);
			intervals = schoolTeaching.get(aid);
			if (intervals == null)
				throw new BuildingDatabaseException("No schedule found for school " + aid);
		}
		return intervals.stream().anyMatch(e -> e.inRange(queryDate));
	}

	public boolean isTeaching(Long id) throws BuildingDatabaseException {
		return isTeaching(id, new Date());
	}

	public boolean isClosed(Long id) throws BuildingDatabaseException {
		return isClosed(id, new Date());
	}

	public boolean isOccupied(Long id) throws BuildingDatabaseException {
		return isOccupied(id, new Date());
	}

	public void forceTokenRefresh() {
		tokenRequest.renewAccessToken();
	}

	public void setToken(String token) {
		metadataAPI.setToken(token);
	}

	/*
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
	*/

	private List<LocalInterval> toIntervalList(Collection<String> strings) {
		List<LocalInterval> intervals = new LinkedList<>();
		for (String s : strings) {
			try {
				LOGGER.debug("Parsing: "+s);
				LocalInterval i = intervalParser.parse(s);
				intervals.add(i);
			} catch (ParseException e) {
				LOGGER.warn("Could not parse: " + s);
			}
		}
		return intervals;
	}

}
