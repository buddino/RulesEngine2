package it.cnit.gaia.rulesengine.service;

import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import net.sargue.mailgun.Response;
import net.sargue.mailgun.content.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class MailService {
	//https://github.com/sargue/mailgun
	Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	//TODO
	String api = "key-12c72acb4d1217567cf3f007290c303b";
	String domain = "sandbox596bc5ced3644ff3a739a710d0c86d11.mailgun.org";
	String frommail = "postmaster@sandbox596bc5ced3644ff3a739a710d0c86d11.mailgun.org";
	String fromname = "Test account";
	String tomail = "cuffaro.giovanni@gmail.com";
	private Configuration configuration;

	public MailService() {
		configuration = new Configuration()
				.domain(domain)
				.apiKey(api)
				.from(fromname, frommail);
	}

	public Response sendMailNotification() {
		return Mail.using(configuration).to(tomail).subject("Prova").text("Hello").build().send();
	}

	@Async
	public Response sendMailNotification(GAIANotification notification, String address) {
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

}
