package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Direct Messages", description = "REST endpoint for loading direct message history")
@RestController
@RequestMapping("/dm")
public class DmController {

    private final ChatService chatService;

    public DmController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "Get the message history for a direct message room")
    @GetMapping("/{dmRoomId}/messages")
    public ResponseEntity<List<Message>> getDmHistory(@PathVariable String dmRoomId) {
        return ResponseEntity.ok(chatService.getDmHistory(dmRoomId));
    }
}
