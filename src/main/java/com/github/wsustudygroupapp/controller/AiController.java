package com.github.wsustudygroupapp.controller;

import com.github.wsustudygroupapp.dto.AiChatRequest;
import com.github.wsustudygroupapp.dto.AiChatResponse;
import com.github.wsustudygroupapp.dto.MessageDTO;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.service.AiService;
import com.github.wsustudygroupapp.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;
    private final ProfileRepository profileRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public AiController(AiService aiService,
                        ProfileRepository profileRepository,
                        SimpMessagingTemplate messagingTemplate,
                        ChatService chatService) {
        this.aiService = aiService;
        this.profileRepository = profileRepository;
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @RequestBody AiChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AiChatResponse aiResponse = aiService.chat(request);

        String senderName = profileRepository.findByUserEmail(userDetails.getUsername())
                .map(p -> p.getName())
                .orElse("Student");

        String topic = "/topic/chat/" + request.getGroupId();

        // Broadcast the user's question so all group members see what was asked
        MessageDTO questionDto = new MessageDTO();
        questionDto.setStudyGroupId(request.getGroupId());
        questionDto.setSenderName(senderName);
        questionDto.setContent("[AI] asked: " + request.getMessage());
        questionDto.setSentAt(LocalDateTime.now());
        messagingTemplate.convertAndSend(topic, questionDto);
        chatService.saveSystemMessage(request.getGroupId(), senderName, questionDto.getContent(), questionDto.getSentAt());

        // Broadcast the AI reply
        LocalDateTime replyTime = LocalDateTime.now();
        MessageDTO replyDto = new MessageDTO();
        replyDto.setStudyGroupId(request.getGroupId());
        replyDto.setSenderName("AI Assistant");
        replyDto.setContent(aiResponse.getReply());
        replyDto.setSentAt(replyTime);
        messagingTemplate.convertAndSend(topic, replyDto);
        chatService.saveSystemMessage(request.getGroupId(), "AI Assistant", aiResponse.getReply(), replyTime);

        return ResponseEntity.ok(aiResponse);
    }
}
