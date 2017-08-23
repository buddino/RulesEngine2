package it.cnit.gaia.rulesengine.service;

import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import net.sargue.mailgun.Response;
import net.sargue.mailgun.content.Body;
import org.springframework.stereotype.Service;


@Service
public class MailService {
	//https://github.com/sargue/mailgun
	private Configuration configuration;

	String api = "key-12c72acb4d1217567cf3f007290c303b";
	String domain = "sandbox596bc5ced3644ff3a739a710d0c86d11.mailgun.org";
	String frommail = "postmaster@sandbox596bc5ced3644ff3a739a710d0c86d11.mailgun.org";
	String fromname = "Test account";
	String tomail = "cuffaro.giovanni@gmail.com";

	public MailService() {
		configuration = new Configuration()
				.domain(domain)
				.apiKey(api)
				.from(fromname, frommail);
	}

	public Response sendMailNotification() {
		return Mail.using(configuration).to(tomail).subject("Prova").text("Hello").build().send();
	}

	public Response sendMailNotification(GAIANotification notification, String address){
		String area = notification.getArea().getName();
		String school = notification.getSchool().getName();
		String suggestion = notification.getSuggestion();
		Body body = Body.builder(configuration)
						.h1(notification.getRuleName()+"-"+notification.getRuleClass())
				.h2(school+"-"+area)
				.strong(suggestion).build();
		return Mail.using(configuration).content(body).to(address).subject("[GAIA Notification]").build().send();
	}

}
