import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
public class TestRule {

	@Before
	public void before(){

	}
	@Test
	public void test() {
		String operator = "=9";
		List<String> validValues = Arrays.asList("==",">","<","<=",">=");
		System.out.println(validValues.stream().anyMatch(v -> v.equals(operator)));

	}
}
