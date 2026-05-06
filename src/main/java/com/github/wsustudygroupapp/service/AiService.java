package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.AiChatRequest;
import com.github.wsustudygroupapp.dto.AiChatResponse;
import com.github.wsustudygroupapp.dto.MessageDTO;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    private final RestTemplate restTemplate;
    private final StudyGroupRepository studyGroupRepository;
    private final ProfileRepository profileRepository;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public AiService(RestTemplate restTemplate,
                     StudyGroupRepository studyGroupRepository,
                     ProfileRepository profileRepository,
                     ChatService chatService,
                     SimpMessagingTemplate messagingTemplate) {
        this.restTemplate = restTemplate;
        this.studyGroupRepository = studyGroupRepository;
        this.profileRepository = profileRepository;
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    public AiChatResponse chat(AiChatRequest request, String userEmail) {
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return new AiChatResponse("AI assistant is not configured. Add an OpenAI key to enable it.");
        }

        String courseContext = studyGroupRepository.findById(request.getGroupId())
                .map(g -> g.getCourse() != null
                        ? g.getCourse().getCourseCode() + " — " + g.getCourse().getCourseName()
                        : "general studies")
                .orElse("general studies");

        // SUPER IMPORTANT, this is the training for the AI agent. This prompt can be adjusted for more accuracy
        String systemPrompt = "You are a helpful AI study assistant for a Westfield State University study group. " +
                "The group is studying " + courseContext + ". " +
                "Help students understand course material, answer academic questions, and explain concepts clearly. " +
                "Keep responses focused, concise, and academic in tone.";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = Map.of(
                "model", openAiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", request.getMessage())
                )
        );

        AiChatResponse aiResponse;
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            aiResponse = new AiChatResponse((String) message.get("content"));
        } catch (Exception e) {
            aiResponse = new AiChatResponse("Sorry, I'm having trouble connecting right now. Please try again in a moment.");
        }

        String senderName = profileRepository.findByUserEmail(userEmail)
                .map(p -> p.getName())
                .orElse("Student");

        String topic = "/topic/chat/" + request.getGroupId();
        LocalDateTime now = LocalDateTime.now();

        MessageDTO questionDto = new MessageDTO();
        questionDto.setStudyGroupId(request.getGroupId());
        questionDto.setSenderName(senderName);
        questionDto.setContent("[AI] asked: " + request.getMessage());
        questionDto.setSentAt(now);
        messagingTemplate.convertAndSend(topic, questionDto);
        chatService.saveSystemMessage(request.getGroupId(), senderName, questionDto.getContent(), now);

        LocalDateTime replyTime = LocalDateTime.now();
        MessageDTO replyDto = new MessageDTO();
        replyDto.setStudyGroupId(request.getGroupId());
        replyDto.setSenderName("AI Assistant");
        replyDto.setContent(aiResponse.getReply());
        replyDto.setSentAt(replyTime);
        messagingTemplate.convertAndSend(topic, replyDto);
        chatService.saveSystemMessage(request.getGroupId(), "AI Assistant", aiResponse.getReply(), replyTime);

        return aiResponse;
    }
}
