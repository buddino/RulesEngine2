package rules;

import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.rules.EnergyWasting;
import it.cnit.gaia.rulesengine.utils.MeasurementsUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.springframework.util.Assert;

import static org.mockito.Mockito.when;

public class TestEnergyWasting extends GenericRuleTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	EnergyWasting rule;

	@Before
	public void setUp() throws RuleInitializationException {
		MockitoAnnotations.initMocks(this);
		rule = new EnergyWasting();
		setUpRule(rule);
	}

	@Test
	public void test1() throws RuleInitializationException {
		rule.power_uri = "123";
		rule.occupancy_uri = "123";
		rule.standby_threshold = 500000.0;
		rule.on_threshold = 1000000.0;
		Assert.isTrue(rule.init());
	}


	@Test
	public void testRoomOccupied() throws RuleInitializationException {
		rule.power_uri = "power";
		rule.occupancy_uri = "occupancy";
		rule.standby_threshold = 500000.0;
		rule.on_threshold = 1000000.0;
		when(measurementRepository.getLatestFor("power")).thenReturn(MeasurementsUtils.getResourceDTO(1600000.0));
		when(measurementRepository.getLatestFor("occupancy")).thenReturn(MeasurementsUtils.getResourceDTO(0.95));
		rule.init();
		Assertions.assertFalse(rule.condition());
	}

	@Test
	public void testRoomEmptyOn() throws RuleInitializationException {
		rule.power_uri = "power";
		rule.occupancy_uri = "occupancy";
		rule.standby_threshold = 500000.0;
		rule.on_threshold = 1000000.0;
		when(measurementRepository.getLatestFor("power")).thenReturn(MeasurementsUtils.getResourceDTO(1600000.0));
		when(measurementRepository.getLatestFor("occupancy")).thenReturn(MeasurementsUtils.getResourceDTO(0.15));
		rule.init();
		Assertions.assertTrue(rule.condition());
	}

	@Test
	public void testRoomEmptyStandby() throws RuleInitializationException {
		rule.power_uri = "power";
		rule.occupancy_uri = "occupancy";
		rule.standby_threshold = 500000.0;
		rule.on_threshold = 1000000.0;
		when(measurementRepository.getLatestFor("power")).thenReturn(MeasurementsUtils.getResourceDTO(600000.0));
		when(measurementRepository.getLatestFor("occupancy")).thenReturn(MeasurementsUtils.getResourceDTO(0.15));
		rule.init();
		Assertions.assertTrue(rule.condition());
	}

	@Test
	public void testRoomEmptyOff() throws RuleInitializationException {
		rule.power_uri = "power";
		rule.occupancy_uri = "occupancy";
		rule.standby_threshold = 500000.0;
		rule.on_threshold = 1000000.0;
		when(measurementRepository.getLatestFor("power")).thenReturn(MeasurementsUtils.getResourceDTO(100000.0));
		when(measurementRepository.getLatestFor("occupancy")).thenReturn(MeasurementsUtils.getResourceDTO(0.15));
		rule.init();
		Assertions.assertFalse(rule.condition());
	}
}
