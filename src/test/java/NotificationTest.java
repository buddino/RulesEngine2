import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnit.gaia.rulesengine.model.Area;
import it.cnit.gaia.rulesengine.model.School;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import it.cnit.gaia.rulesengine.model.notification.NotificationType;
import org.junit.Test;

public class NotificationTest {
	@Test
	public void test() throws JsonProcessingException {
		School s = new School();
		s.setAid(0L);

		Area a = new Area().setAid(1L).setSchool(s);
		Area a1 = new Area().setAid(10L).setSchool(s);
		a.add(new Area().setAid(2L).setSchool(s));
		a.add(new Area().setAid(3L).setSchool(s));
		a1.add(new Area().setAid(4L).setSchool(s));
		a.add(new Area().setAid(5L).setSchool(s));
		a1.add(new Area().setAid(6L).setSchool(s));

		GAIANotification notification = new GAIANotification();

		notification.setArea(a1);
		notification.setSchool(s);
		notification.setTimestamp(0L);
		notification.setRuleClass("dsf");
		notification.setSuggestion("sdfsd");
		notification.setType(NotificationType.success);

		ObjectMapper mapper = new ObjectMapper();
		String mapped = mapper.writeValueAsString(notification);

		System.out.println(mapped);
	}
}
