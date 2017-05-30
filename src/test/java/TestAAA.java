import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = TestAAA.class)
public class TestAAA {

	SparksTokenRequest tokenRequest;

	@Test
	public void testAccessTokenRequest() {
		tokenRequest = new SparksTokenRequest("gaia-prato","cmRxm2","c9ce97aa-3b71-446e-b3f4-f7130dfddb32","CNIT");
		Assert.notNull(tokenRequest.getAccessToken());
	}

	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void testAccessWrongTokenRequest() {
		tokenRequest = new SparksTokenRequest("gaia-prato","wrongpassword","c9ce97aa-3b71-446e-b3f4-f7130dfddb32","CNIT");
	}

	@Test
	public void testRefreshToken() throws IOException {
		tokenRequest = new SparksTokenRequest("gaia-prato","cmRxm2","c9ce97aa-3b71-446e-b3f4-f7130dfddb32","CNIT");
		String token = tokenRequest.getAccessToken();
		System.out.println(token);
		tokenRequest.refreshToken();
		String refreshedToken = tokenRequest.getAccessToken();
		System.out.println(refreshedToken);
		Assert.isTrue(!token.equals(refreshedToken));
	}
}
