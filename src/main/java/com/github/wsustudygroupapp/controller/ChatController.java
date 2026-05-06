package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.DmPresenceDTO;
import com.github.wsustudygroupapp.dto.MessageDTO;
import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.service.ActiveDmTracker;
import com.github.wsustudygroupapp.service.ChatService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;

// TODO: Brian — WebSocket message handler
// This is NOT a @RestController — it uses @MessageMapping for WebSocket (STOMP)
// When a client sends a message to /app/chat/{groupId}, this method handles it
// The response is broadcast to everyone subscribed to /topic/chat/{groupId}

@Controller
public class ChatController {

    private final ChatService chatService;
    private final ActiveDmTracker activeDmTracker;

    public ChatController(ChatService chatService, ActiveDmTracker activeDmTracker) {
        this.chatService = chatService;
        this.activeDmTracker = activeDmTracker;
    }

    // TODO: set the sentAt timestamp on the incoming message DTO
    // TODO: call chatService.saveMessage(dto) to persist it
    // TODO: return the dto — Spring will broadcast it to /topic/chat/{groupId}
    @MessageMapping("/chat/{groupId}")
    @SendTo("/topic/chat/{groupId}")
    public MessageDTO sendMessage(MessageDTO dto) {
        dto.setSentAt(LocalDateTime.now());
        chatService.saveMessage(dto);
        return dto;
    }

    @MessageMapping("/dm/{dmRoomId}")
    @SendTo("/topic/dm/{dmRoomId}")
    public MessageDTO sendDirectMessage(MessageDTO dto) {
        dto.setSentAt(LocalDateTime.now());
        chatService.saveMessage(dto);
        return dto;
    }

    @MessageMapping("/dm/enter/{dmRoomId}")
    public void enterDm(SimpMessageHeaderAccessor headerAccessor,
                        @DestinationVariable String dmRoomId,
                        DmPresenceDTO dto) {
        activeDmTracker.enter(headerAccessor.getSessionId(), dto.getProfileId(), dmRoomId);
    }

    @MessageMapping("/dm/leave/{dmRoomId}")
    public void leaveDm(SimpMessageHeaderAccessor headerAccessor,
                        @DestinationVariable String dmRoomId) {
        activeDmTracker.leave(headerAccessor.getSessionId());
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        activeDmTracker.leave(event.getSessionId());
    }
}
