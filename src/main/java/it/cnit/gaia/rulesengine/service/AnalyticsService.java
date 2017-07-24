package it.cnit.gaia.rulesengine.service;

import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class AnalyticsService implements SparksAAAService{
	@Autowired
	MeasurementRepository measurement;

	@Autowired
	SparksTokenRequest tokenRequest;

	@PostConstruct
	public void init(){
		tokenRequest.registerService(this);
	}

	@Override
	public void setToken(String token) {

	}

	public void forceTokenRefresh(){
		tokenRequest.renewAccessToken();
	}
}
