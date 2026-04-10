package com.github.wsustudygroupapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// TODO: Brian — WebSocket configuration
// This sets up the STOMP WebSocket broker that powers the real-time group chat
//
// How it works:
//  - Frontend connects to /ws to open a WebSocket connection
//  - Frontend sends messages to /app/chat/{groupId}
//  - Backend broadcasts responses to /topic/chat/{groupId}
//  - All members subscribed to /topic/chat/{groupId} receive the message instantly

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
