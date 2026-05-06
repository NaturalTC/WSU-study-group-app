package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.MessageDTO;
import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;

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
        dto.setSentAt(LocalDateTime.now());
        chatService.saveMessage(dto);
        return dto;
    }

    @GetMapping("/chat/{groupId}/history")
    @ResponseBody
    public List<Message> getGroupHistory(@PathVariable Long groupId) {
        return chatService.getHistory(groupId);
    }

    // Frontend connects to /topic/dm/{dmRoomId} and sends to /app/dm/{dmRoomId}
    // dmRoomId is computed by the frontend as "dm-" + Math.min(id1,id2) + "-" + Math.max(id1,id2)
    @MessageMapping("/dm/{dmRoomId}")
    @SendTo("/topic/dm/{dmRoomId}")
    public MessageDTO sendDm(MessageDTO dto) {
        dto.setSentAt(LocalDateTime.now());
        chatService.saveMessage(dto);
        return dto;
    }

    @GetMapping("/dm/{dmRoomId}/history")
    @ResponseBody
    public List<Message> getDmHistory(@PathVariable String dmRoomId) {
        return chatService.getDmHistory(dmRoomId);
    }
}
