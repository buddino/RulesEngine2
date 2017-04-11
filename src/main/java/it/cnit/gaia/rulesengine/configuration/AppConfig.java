package it.cnit.gaia.rulesengine.configuration;

//For JUnit testing

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"it.cnit.gaia.rulesengine"})
public class AppConfig {

}
