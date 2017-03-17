package it.cnit.gaia.rulesengine.configuration;

import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

public class WebSocketAAA extends AbstractSecurityWebSocketMessageBrokerConfigurer {
	protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
		messages.simpDestMatchers("/user/*").authenticated();
	}
}
