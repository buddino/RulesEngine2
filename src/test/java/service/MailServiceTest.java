package service;

import io.swagger.sparks.ApiException;
import it.cnit.gaia.rulesengine.service.MailService;
import net.sargue.mailgun.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MailService.class})
public class MailServiceTest {

	String tomail = "cuffaro.giovanni@gmail.com";

	@Autowired
	MailService mailService;

	@Before
	public void setup() {
	}

	@Test
	public void testSendingMail() throws ApiException {
		Response response = mailService.sendMailNotification(tomail);
		System.out.println(response.responseMessage());
	}



}
