package it.cnit.gaia.rulesengine.service;

public interface SparksAAAService {
	void setToken(String token);
	void forceTokenRefresh();
}
