package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;

import java.util.ArrayList;
import java.util.List;

public class PowerFactor extends GaiaRule{

	@LogMe
	@LoadMe
	@URI
	public String pwf_uri;	//If 3-Phase this is the base uri, /1 /2 /3 will be added automatically

	@LoadMe( required = false)
	public int n_phases = 1;

	@LogMe
	@LoadMe( required = false)
	public Double pwf_threshold = 0.7;

	@LogMe
	public List<Double> pwf_value;

	@Override
	public boolean condition() {
		pwf_value = new ArrayList<>();
		if(n_phases==1){
			try {
				pwf_value.add(measurements.getLatestFor(pwf_uri).getReading());
			}
			catch (NullPointerException e){LOGGER.error("["+rid+"]"+e.getMessage());}
			return pwf_value.get(0) < pwf_threshold;
		}
		else if(n_phases==3){
			try {
				pwf_value.add(measurements.getLatestFor(pwf_uri + "/1").getReading());
				pwf_value.add(measurements.getLatestFor(pwf_uri + "/2").getReading());
				pwf_value.add(measurements.getLatestFor(pwf_uri + "/3").getReading());
				}
			catch (NullPointerException e){ LOGGER.error("["+rid+"]"+e.getMessage());}
			return pwf_value.stream().anyMatch( value -> value < pwf_threshold);
		}
		else {
			LOGGER.warn("Illegal number of phases");
			return false;
		}
	}

}
