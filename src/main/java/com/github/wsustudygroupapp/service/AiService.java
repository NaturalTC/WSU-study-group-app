package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.AiChatRequest;
import com.github.wsustudygroupapp.dto.AiChatResponse;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    private final StudyGroupRepository studyGroupRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public AiService(StudyGroupRepository studyGroupRepository) {
        this.studyGroupRepository = studyGroupRepository;
    }

    public AiChatResponse chat(AiChatRequest request) {
        String courseContext = studyGroupRepository.findById(request.getGroupId())
                .map(g -> g.getCourse() != null
                        ? g.getCourse().getCourseCode() + " — " + g.getCourse().getCourseName()
                        : "general studies")
                .orElse("general studies");

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
            return new AiChatResponse((String) message.get("content"));
        } catch (Exception e) {
            return new AiChatResponse("Sorry, I'm having trouble connecting right now. Please try again in a moment.");
        }
    }
}
