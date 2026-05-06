package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.AiChatRequest;
import com.github.wsustudygroupapp.dto.AiChatResponse;
import com.github.wsustudygroupapp.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @RequestBody AiChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(aiService.chat(request, userDetails.getUsername()));
    }
}
