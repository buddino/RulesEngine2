package it.cnit.gaia.rulesengine.service;

import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import net.sargue.mailgun.Response;
import net.sargue.mailgun.content.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;


@Service
@PropertySource("file:mailgun.properties")
public class MailService {
	//https://github.com/sargue/mailgun
	Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	@Value("${mailgun.api_key}")
	String api_key;
	@Value("${mailgun.domain}")
	String domain;
	@Value("${mailgun.from_address}")
	String frommail;
	@Value("${mailgun.from_name}")
	String fromname;

	int mailSent = 0;
	final int MAXMAIL = 30;  //Max mail per hour
	Date latest = new Date();

	private Configuration configuration;

	@PostConstruct
	public void injectConfiguration(){
		configuration = new Configuration()
				.domain(domain)
				.apiKey(api_key)
				.from(fromname, frommail);
	}

	public Response sendMailNotification(String address) {
		return Mail.using(configuration).to(address).subject("Prova").text("Hello").build().send();
	}

	private boolean checkTimerange(){
		Date now = new Date();
		if (now.getTime() - latest.getTime() > 3600*1000){
			//An hour has passed
			latest = new Date();
			mailSent = 0;
			return false;
		}
		return true;
	}

	@Async
	public Response sendMailNotification(GAIANotification notification, String address) {
		if(checkTimerange()) {
			if (++mailSent > MAXMAIL) {
				LOGGER.error("MAX EMAIL LIMIT REACHED ("+MAXMAIL+"). Mail not sent to: "+address);
			}
		}
		LOGGER.debug("Sending email to: " + address);
		String area = notification.getArea().getName();
		String school = notification.getSchool().getName();
		String suggestion = notification.getSuggestion();

		Body body = Body.builder(configuration)
						.h1("Rule:" + notification.getRuleName() + "(" + notification.getRuleClass() + ")")
						.h2("Area: " + area + "(" + school + ")")
						.strong("Suggestion: " + suggestion).br().text(notification.getValues().toString()).build();
		Response response = Mail.using(configuration).content(body).to(address).subject("[GAIA Notification]").build()
								.send();
		if (response.isOk())
			LOGGER.debug("Succesfully sent email to: " + address);
		else
			LOGGER.warn(response.responseMessage());
		return response;
	}

	public Response sendMailNotificationMock(GAIANotification notification, String address) {
		if(checkTimerange()) {
			if (++mailSent > MAXMAIL) {
				LOGGER.error("MAX EMAIL LIMIT REACHED ("+MAXMAIL+"). Mail not sent to: "+address);
			}
		}
		LOGGER.info("Sending email to: " + address);
		//Response response = Mail.using(configuration).content(body).to(address).subject("[GAIA Notification]").build().send();
		return null;
	}

}
