package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.MessageDTO;
import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

// TODO: Brian — WebSocket message handler
// This is NOT a @RestController — it uses @MessageMapping for WebSocket (STOMP)
// When a client sends a message to /app/chat/{groupId}, this method handles it
// The response is broadcast to everyone subscribed to /topic/chat/{groupId}

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // TODO: set the sentAt timestamp on the incoming message DTO
    // TODO: call chatService.saveMessage(dto) to persist it
    // TODO: return the dto — Spring will broadcast it to /topic/chat/{groupId}
    @MessageMapping("/chat/{groupId}")
    @SendTo("/topic/chat/{groupId}")
    public MessageDTO sendMessage(MessageDTO dto) {
        // TODO: dto.setSentAt(LocalDateTime.now())
        LocalDateTime sentAt = LocalDateTime.now();
        dto.setSentAt(sentAt);
        // TODO: chatService.saveMessage(dto)
        chatService.saveMessage(dto);
        // TODO: return dto
        return dto;
    }
}
