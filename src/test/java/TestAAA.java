import it.cnit.gaia.rulesengine.configuration.RestControllerAAA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestControllerAAA.class)
public class TestAAA {

	@Autowired
	RemoteTokenServices tokenService;

	@Test
	public void test() {
		OAuth2Authentication oAuth2Authentication = tokenService.loadAuthentication("28f48152-c86e-42a6-b2de-58e2f7de4dc5");
		System.out.println(oAuth2Authentication.toString());
	}
}
