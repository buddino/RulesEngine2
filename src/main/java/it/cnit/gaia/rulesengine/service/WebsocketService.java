package it.cnit.gaia.rulesengine.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketService {
	Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	@Autowired
	private ObjectMapper mapper;
	private SimpMessagingTemplate template;

	@Autowired
	public WebsocketService(SimpMessagingTemplate template) {
		this.template = template;
	}

	public void pushNotification(String message) {
		this.template.convertAndSend("/recommendations", message);
	}

	public void pushNotification(GAIANotification notification) {
		try {
			String message = mapper.writeValueAsString(notification);
			String destination = "/recommendations/" + notification.getSchool().aid;
			this.template.convertAndSend(destination, message);
			LOGGER.debug(String.format("%s (%d)\tRule: %s (%s)",
					notification.getSchool().getName(),
					notification.getSchool().aid,
					notification.getRuleId(),
					notification.getRuleClass()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			LOGGER.error(e.getMessage(), e.getCause());
		}
	}

	public void pushSensorWarning(Long id, String uri, String info) {
		ObjectNode objectNode = mapper.createObjectNode();
		objectNode.put("id", id)
				  .put("power_uri", uri)
				  .put("info", info);
		try {
			String message = mapper.writeValueAsString(objectNode);
			this.template.convertAndSend("/warnings", message);
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage(),e);
		}

	}


}
