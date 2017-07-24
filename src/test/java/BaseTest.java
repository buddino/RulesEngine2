import org.junit.Test;

import java.time.ZoneId;
import java.util.TimeZone;

public class BaseTest {
@Test
	public void test(){
	TimeZone timeZone = TimeZone.getTimeZone("Europe/Rome");
	System.out.println(timeZone);
	ZoneId of = ZoneId.of("Europe/Rome");
	System.out.println(of.getRules());
}
}
