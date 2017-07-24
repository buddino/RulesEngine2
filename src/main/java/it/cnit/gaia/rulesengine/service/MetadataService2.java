package it.cnit.gaia.rulesengine.service;

import io.swagger.metadata.api.*;
import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MetadataService2 implements SparksAAAService{
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Autowired
	SparksTokenRequest tokenRequest;

	//API Services
	private EventEntityApi eventEntityApi = new EventEntityApi();
	private MaintenanceWorkEntityApi maintenanceWorkEntityApi = new MaintenanceWorkEntityApi();
	private ScheduleEntityApi scheduleEntityApi = new ScheduleEntityApi();
	private SiteInfoEntityApi siteInfoEntityApi = new SiteInfoEntityApi();
	private SiteManagerEntityApi siteManagerEntityApi = new SiteManagerEntityApi();
	private SiteUtilityAPIApi siteUtilityAPIApi = new SiteUtilityAPIApi();

	@PostConstruct
	public void init(){
		tokenRequest.registerService(this);
	}

	public void forceTokenRefresh(){
		tokenRequest.renewAccessToken();
	}

	public void setToken(String token){
		eventEntityApi.getApiClient().setToken(token);
		maintenanceWorkEntityApi.getApiClient().setToken(token);
		scheduleEntityApi.getApiClient().setToken(token);
		siteInfoEntityApi.getApiClient().setToken(token);
		siteManagerEntityApi.getApiClient().setToken(token);
		siteUtilityAPIApi.getApiClient().setToken(token);
	}








	public SparksTokenRequest getTokenRequest() {
		return tokenRequest;
	}

	public EventEntityApi getEventEntityApi() {
		return eventEntityApi;
	}

	public MaintenanceWorkEntityApi getMaintenanceWorkEntityApi() {
		return maintenanceWorkEntityApi;
	}

	public ScheduleEntityApi getScheduleEntityApi() {
		return scheduleEntityApi;
	}

	public SiteInfoEntityApi getSiteInfoEntityApi() {
		return siteInfoEntityApi;
	}

	public SiteManagerEntityApi getSiteManagerEntityApi() {
		return siteManagerEntityApi;
	}

	public SiteUtilityAPIApi getSiteUtilityAPIApi() {
		return siteUtilityAPIApi;
	}
}
