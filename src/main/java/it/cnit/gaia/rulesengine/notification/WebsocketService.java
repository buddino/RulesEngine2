package it.cnit.gaia.rulesengine.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnit.gaia.rulesengine.model.notification.GAIANotification;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketService {
    Logger LOGGER = Logger.getLogger(this.getClass().getSimpleName());
    @Autowired
    private ObjectMapper mapper;
    private SimpMessagingTemplate template;

    @Autowired
    public WebsocketService(SimpMessagingTemplate template) {
        this.template = template;
    }

    public void pushNotification(String message) {
        this.template.convertAndSend("/topic/recommendations", message);
    }
    public void pushNotification(GAIANotification notification) {
        try {
            String message = mapper.writeValueAsString(notification);
            this.template.convertAndSend("/topic/recommendations", message);
            LOGGER.debug("\u001B[36mWS NOTIFICATION\u001B[0m\t" + notification.toString());


        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
