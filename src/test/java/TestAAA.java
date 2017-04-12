import it.cnit.gaia.rulesengine.configuration.SparksTokenRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest( classes = TestAAA.class)
public class TestAAA {

	SparksTokenRequest tokenRequest;

	@Test
	public void testAccessTokenRequest() {
		tokenRequest = new SparksTokenRequest("gaia-prato","cmRxm2","c9ce97aa-3b71-446e-b3f4-f7130dfddb32","CNIT");
		Assert.notNull(tokenRequest.getAccess_token());
	}

	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void testAccessWrongTokenRequest() {
		tokenRequest = new SparksTokenRequest("gaia-prato","wrongpassword","c9ce97aa-3b71-446e-b3f4-f7130dfddb32","CNIT");
	}

	@Test
	public void testRefreshToken(){
		tokenRequest = new SparksTokenRequest("gaia-prato","cmRxm2","c9ce97aa-3b71-446e-b3f4-f7130dfddb32","CNIT");
		String token = tokenRequest.getAccess_token();
		System.out.println(token);
		tokenRequest.refreshToken();
		String refreshedToken = tokenRequest.getAccess_token();
		System.out.println(refreshedToken);
		Assert.isTrue(!token.equals(refreshedToken));
	}
}
