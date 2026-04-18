package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.AiChatRequest;
import com.github.wsustudygroupapp.dto.AiChatResponse;
import com.github.wsustudygroupapp.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// TODO: Jose Jimenez — exposes the AI study assistant endpoint
// All routes here require a valid JWT token

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    // TODO: call aiService.chat(request)
    // TODO: return 200 with the AiChatResponse
    // NOTE: the frontend also broadcasts this reply over WebSocket to all group members
    //       so every student in the session sees the AI's answer — not just the one who asked
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        return null;
    }
}
