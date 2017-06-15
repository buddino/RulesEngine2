package rules;

import it.cnit.gaia.rulesengine.api.request.EventDTO;
import it.cnit.gaia.rulesengine.model.event.GaiaEvent;
import it.cnit.gaia.rulesengine.model.exceptions.RuleInitializationException;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import it.cnit.gaia.rulesengine.rules.EnergyWasting;
import it.cnit.gaia.rulesengine.utils.MeasurementsUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.springframework.util.Assert;

import java.util.List;

import static org.mockito.Mockito.*;

public class TestEnergyWasting extends GenericRuleTest {
	private final Double SB_TH = 1.0E5;
	private final Double SB = 2.0E5;
	private final Double ON_TH = 1.0E6;
	private final Double ON = 2.0E6;
	private final Double OFF = 2.0E3;
	private final Double OCCUPIED = 0.9;
	private final Double EMPTY = 0.1;
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
	public void testNotValidInitializationMissinPowerURI() throws RuleInitializationException {
		thrown.expect(RuleInitializationException.class);
		//rule.power_uri = "123";
		rule.occupancy_uri = "123";
		rule.standby_threshold = SB_TH;
		rule.on_threshold = ON_TH;
		Assert.isTrue(rule.init());
	}

	@Test
	public void testValidInitializationMissingOccupancyURI() throws RuleInitializationException {
		rule.power_uri = "123";
		//rule.occupancy_uri = "123";
		rule.standby_threshold = SB_TH;
		rule.on_threshold = ON_TH;
		Assert.isTrue(rule.init());
	}

	@Test
	public void testNotValidInitializationMissingSbThreshold() throws RuleInitializationException {
		thrown.expect(RuleInitializationException.class);
		rule.power_uri = "123";
		rule.occupancy_uri = "123";
		//rule.standby_threshold = SB_TH;
		rule.on_threshold = ON_TH;
		rule.init();
	}

	@Test
	public void testNotValidInitializationMissingOnThreshold() throws RuleInitializationException {
		thrown.expect(RuleInitializationException.class);
		rule.power_uri = "123";
		rule.occupancy_uri = "123";
		rule.standby_threshold = SB_TH;
		//rule.on_threshold = ON_TH;
		rule.init();
	}


	@Test
	public void testValidInitialization() throws RuleInitializationException {
		rule.power_uri = "123";
		rule.occupancy_uri = "123";
		rule.standby_threshold = SB_TH;
		rule.on_threshold = ON_TH;
		Assert.isTrue(rule.init());
	}


	@Test
	public void testRoomOccupied() throws RuleInitializationException {
		rule.power_uri = "power";
		rule.occupancy_uri = "occupancy";
		rule.standby_threshold = SB_TH;
		rule.on_threshold = ON_TH;
		when(measurementRepository.getLatestFor("power")).thenReturn(MeasurementsUtils.getResourceDTO(ON));
		when(measurementRepository.getLatestFor("occupancy")).thenReturn(MeasurementsUtils.getResourceDTO(OCCUPIED));
		rule.init();
		Assertions.assertFalse(rule.condition());
	}

	@Test
	public void testRoomEmptyOn() throws RuleInitializationException {
		rule.power_uri = "power";
		rule.occupancy_uri = "occupancy";
		rule.standby_threshold = SB_TH;
		rule.on_threshold = ON_TH;
		when(measurementRepository.getLatestFor("power")).thenReturn(MeasurementsUtils.getResourceDTO(ON));
		when(measurementRepository.getLatestFor("occupancy")).thenReturn(MeasurementsUtils.getResourceDTO(EMPTY));
		rule.init();
		rule.fire();

		//Should log the event and send notification
		verify(eventService, times(1)).addEvent(any(GaiaEvent.class));
		verify(websocketService, times(1)).pushNotification(any(GAIANotification.class));
	}

	@Test
	public void testRoomEmptyStandby() throws RuleInitializationException {
		rule.power_uri = "power";
		rule.occupancy_uri = "occupancy";
		rule.standby_threshold = SB_TH;
		rule.on_threshold = ON_TH;
		when(measurementRepository.getLatestFor("power")).thenReturn(MeasurementsUtils.getResourceDTO(SB));
		when(measurementRepository.getLatestFor("occupancy")).thenReturn(MeasurementsUtils.getResourceDTO(EMPTY));
		rule.init();
		rule.fire();
		//Should log the event but should not send any notification
		//Devices in standby, rule fired for the first time (default threshold is 3 time in an hour)
		verify(eventService, times(1)).addEvent(any(GaiaEvent.class));
		verify(websocketService, never()).pushNotification(any(GAIANotification.class));
	}

	@Test
	public void testRoomEmptyOff() throws RuleInitializationException {
		rule.power_uri = "power";
		rule.occupancy_uri = "occupancy";
		rule.standby_threshold = SB_TH;
		rule.on_threshold = ON_TH;
		when(measurementRepository.getLatestFor("power")).thenReturn(MeasurementsUtils.getResourceDTO(OFF));
		when(measurementRepository.getLatestFor("occupancy")).thenReturn(MeasurementsUtils.getResourceDTO(EMPTY));
		rule.init();
		rule.fire();
		//Should not invoke action(), so no notification sent or event logged
		verify(eventService, never()).addEvent(any(GaiaEvent.class));
		verify(websocketService, never()).pushNotification(any(GAIANotification.class));
	}

	@Test
	public void testRepeatedFiresInStandby() throws RuleInitializationException {
		rule.power_uri = "power";
		rule.occupancy_uri = "occupancy";
		rule.standby_threshold = SB_TH;
		rule.on_threshold = ON_TH;
		when(measurementRepository.getLatestFor("power")).thenReturn(MeasurementsUtils.getResourceDTO(SB));
		when(measurementRepository.getLatestFor("occupancy")).thenReturn(MeasurementsUtils.getResourceDTO(EMPTY));
		List<EventDTO> mockList = mock(List.class);
		when(eventService.getLatestEventsForRule(anyString(), anyLong(), anyLong())).thenReturn(mockList);
		rule.init();
		//Fire the rule times+1 times
		for (int i = 0; i < 4; i++) {
			when(mockList.size()).thenReturn(i + 1);
			rule.fire();
		}
		//times+1 logged events and 1 notification
		verify(eventService, times(rule.times + 1)).addEvent(any(GaiaEvent.class));
		verify(websocketService, times(1)).pushNotification(any(GAIANotification.class));
	}


	@Test
	public void testFireMissingOccupancyURI() throws RuleInitializationException {
		rule.power_uri = "power";
		rule.standby_threshold = SB_TH;
		rule.on_threshold = ON_TH;
		when(measurementRepository.getLatestFor("power")).thenReturn(MeasurementsUtils.getResourceDTO(ON));
		rule.init();
		//Should not be invoked, isOccupied() returns true
		//TODO Missing ScheduleService
		verify(eventService, never()).addEvent(any(GaiaEvent.class));
		verify(websocketService, never()).pushNotification(any(GAIANotification.class));
	}
}
