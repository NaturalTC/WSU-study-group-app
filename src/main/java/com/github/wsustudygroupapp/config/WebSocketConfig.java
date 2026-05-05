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

@Configuration // tells Spring to run this class on startup as part of app setup
@EnableWebSocketMessageBroker // activates the full WebSocket + STOMP messaging system
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer { // lets us override the two setup methods below

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic"); // starts an in-memory broker — messages sent to /topic/... are broadcast to all subscribers
        registry.setApplicationDestinationPrefixes("/app"); // messages sent to /app/... are routed to @MessageMapping controller methods
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // the URL the frontend connects to to open a WebSocket connection
                .setAllowedOriginPatterns("*") // allows connections from any domain (fine for dev, should be restricted in production)
                .withSockJS(); // adds a fallback for browsers that don't support native WebSockets
    }
}
