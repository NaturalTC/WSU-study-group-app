package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.model.Message;
import com.github.wsustudygroupapp.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dm")
public class DmController {

    private final ChatService chatService;

    public DmController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{dmRoomId}/messages")
    public ResponseEntity<List<Message>> getDmHistory(@PathVariable String dmRoomId) {
        return ResponseEntity.ok(chatService.getDmHistory(dmRoomId));
    }
}
