package it.cnit.gaia.rulesengine.rules;

import it.cnit.gaia.rulesengine.model.GaiaRule;
import it.cnit.gaia.rulesengine.model.annotation.LoadMe;
import it.cnit.gaia.rulesengine.model.annotation.LogMe;
import it.cnit.gaia.rulesengine.model.annotation.URI;

public class PowerFactor extends GaiaRule{

	@LogMe
	@LoadMe
	@URI
	public String meter_uri;	//If 3-Phase this is the base uri, /1 /2 /3 will be added automatically

	@LoadMe
	public int n_phases = 1;

	@LogMe
	@LoadMe
	public Double pwf_threshold = 0.7;

	@LogMe
	public Double[] pwf_value;

	@Override
	public boolean condition() {
		if(n_phases==1){
			pwf_value = new Double[1];
			pwf_value[0] = measurements.getLatestFor(meter_uri).getReading();
			return pwf_value[0] < pwf_threshold;
		}
		else if(n_phases==3){
			pwf_value = new Double[3];
			pwf_value[0] = measurements.getLatestFor(meter_uri+"/1").getReading();
			pwf_value[1] = measurements.getLatestFor(meter_uri+"/2").getReading();
			pwf_value[2] = measurements.getLatestFor(meter_uri+"/3").getReading();
			return pwf_value[0] < pwf_threshold || pwf_value[1] < pwf_threshold || pwf_value[2] < pwf_threshold;
		}
		else {
			LOGGER.warn("Illegal number of phases");
			return false;
		}
	}

}
