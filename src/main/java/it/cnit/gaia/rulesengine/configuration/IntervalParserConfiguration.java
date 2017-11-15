package it.cnit.gaia.rulesengine.configuration;

import it.cnit.gaia.intervalparser.LocalIntervalParser;
import it.cnit.gaia.intervalparser.ZonedIntervalParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IntervalParserConfiguration {
	@Bean
	LocalIntervalParser getLocalIntervalParser(){
		return new LocalIntervalParser();
	}

	@Bean
	ZonedIntervalParser getZonedIntervalParser(){
		return new ZonedIntervalParser();
	}
}
