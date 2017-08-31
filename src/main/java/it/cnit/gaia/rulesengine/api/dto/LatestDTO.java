package it.cnit.gaia.rulesengine.api.dto;

import java.util.Date;

public class LatestDTO <T> {
	Date latest;
	T data;

	public Date getLatest() {
		return latest;
	}

	public LatestDTO setLatest(Date latest) {
		this.latest = latest;
		return this;
	}

	public T getData() {
		return data;
	}

	public LatestDTO setData(T data) {
		this.data = data;
		return this;
	}
}
