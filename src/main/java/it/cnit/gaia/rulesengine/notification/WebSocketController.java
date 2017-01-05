package it.cnit.gaia.rulesengine.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    Logger LOGGER = Logger.getLogger(this.getClass());
    @Autowired
    private ObjectMapper mapper;
    private SimpMessagingTemplate template;

    @Autowired
    public WebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void pushNotification(String message) {
        this.template.convertAndSend("/recommendation", message);
    }
    public void pushNotification(GAIANotification notification) {
        try {
            String message = mapper.writeValueAsString(notification);
            LOGGER.info("SEND: "+message);
            this.template.convertAndSend("/recommendation", message);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
