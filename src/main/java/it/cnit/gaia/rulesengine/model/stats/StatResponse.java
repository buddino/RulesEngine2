package it.cnit.gaia.rulesengine.model.stats;

import java.util.HashMap;
import java.util.Map;

public class StatResponse {
	private Map<String,Long> count = new HashMap<>();

	public Map<String, Long> getCount() {
		return count;
	}

	public void setCount(Long count){
		this.count.put("count",count);
	}

	public void put(String key, Long value){
		count.put(key,value);
	}
}
