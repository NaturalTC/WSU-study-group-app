package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.AiChatRequest;
import com.github.wsustudygroupapp.dto.AiChatResponse;
import com.github.wsustudygroupapp.dto.MessageDTO;
import com.github.wsustudygroupapp.repository.ProfileRepository;
import com.github.wsustudygroupapp.repository.StudyGroupRepository;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AiService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    private final StudyGroupRepository studyGroupRepository;
    private final ProfileRepository profileRepository;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // The OpenAI client — created once at startup in @PostConstruct
    // null if no API key is configured
    private OpenAIClient openAiClient;

    public AiService(StudyGroupRepository studyGroupRepository,
                     ProfileRepository profileRepository,
                     ChatService chatService,
                     SimpMessagingTemplate messagingTemplate) {
        this.studyGroupRepository = studyGroupRepository;
        this.profileRepository    = profileRepository;
        this.chatService          = chatService;
        this.messagingTemplate    = messagingTemplate;
    }

    // @PostConstruct runs once after Spring has injected all @Value fields
    // We can't create the client in the constructor because @Value fields
    // aren't available yet at construction time — Spring injects them after
    @PostConstruct
    public void init() {
        if (openAiApiKey != null && !openAiApiKey.isBlank()) {
            openAiClient = OpenAIOkHttpClient.builder()
                    .apiKey(openAiApiKey)
                    .build();
            log.info("OpenAI client initialized with model={}", openAiModel);
        } else {
            log.warn("OpenAI API key not configured — AI assistant will be disabled");
        }
    }

    public AiChatResponse chat(AiChatRequest request, String userEmail) {
        log.info("chat called by userEmail={} for groupId={}", userEmail, request.getGroupId());

        // If no API key was configured, bail out early with a friendly message
        if (openAiClient == null) {
            log.warn("chat rejected — OpenAI client not initialized");
            return new AiChatResponse("AI assistant is not configured. Add an OpenAI key to enable it.");
        }

        // Look up the course name so the AI knows what subject it's helping with
        // e.g. "CAIS 0236 — Computer Organization"
        // Falls back to "general studies" if the group or course isn't found
        String courseContext = studyGroupRepository.findById(request.getGroupId())
                .map(g -> g.getCourse() != null
                        ? g.getCourse().getCourseCode() + " — " + g.getCourse().getCourseName()
                        : "general studies")
                .orElse("general studies");

        // The system prompt is the behind-the-scenes instructions sent to OpenAI
        // It tells the AI how to behave — the student never sees this
        // SUPER IMPORTANT: this is the training for the AI agent. Adjust for more accuracy
        String systemPrompt = "You are a helpful AI study assistant for a Westfield State University study group. " +
                "The group is studying " + courseContext + ". " +
                "Help students understand course material, answer academic questions, and explain concepts clearly. " +
                "Keep responses focused, concise, and academic in tone.";

        // Build the request — tell the SDK which model to use, give it the system prompt
        // and the student's message. The SDK handles headers, JSON, and HTTP internally.
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(openAiModel)
                .addSystemMessage(systemPrompt)       // hidden instructions to the AI
                .addUserMessage(request.getMessage()) // what the student actually typed
                .build();

        // Call OpenAI and get the reply
        // Declared outside try so it's accessible after the block whether it succeeds or fails
        AiChatResponse aiResponse;
        try {
            // The SDK makes the HTTP call, parses the JSON response, and returns a typed object
            ChatCompletion completion = openAiClient.chat().completions().create(params);

            // Pull the actual text out of the first choice OpenAI returned
            // orElse handles the rare case where content is empty
            String reply = completion.choices().get(0).message().content().orElse("No response");
            aiResponse = new AiChatResponse(reply);

        } catch (Exception e) {
            // If OpenAI is down, the key is wrong, or a timeout occurs — don't crash
            // Just return a friendly error message to the student
            log.error("chat OpenAI call failed for groupId={}: {}", request.getGroupId(), e.getMessage());
            aiResponse = new AiChatResponse("Sorry, I'm having trouble connecting right now. Please try again in a moment.");
        }

        // Look up the student's display name from their email
        // Shown in chat next to "[AI] asked: ..."
        String senderName = profileRepository.findByUserEmail(userEmail)
                .map(p -> p.getName())
                .orElse("Student");

        // The WebSocket topic for this group — everyone with the chat open is subscribed here
        String topic = "/topic/chat/" + request.getGroupId();
        LocalDateTime now = LocalDateTime.now();

        // Broadcast the student's question to the group chat live via WebSocket
        // then save it to the DB so it shows in history when someone opens the chat later
        MessageDTO questionDto = new MessageDTO();
        questionDto.setStudyGroupId(request.getGroupId());
        questionDto.setSenderName(senderName);
        questionDto.setContent("[AI] asked: " + request.getMessage());
        questionDto.setSentAt(now);
        messagingTemplate.convertAndSend(topic, questionDto);
        chatService.saveSystemMessage(request.getGroupId(), senderName, questionDto.getContent(), now);

        // Broadcast the AI's reply to the group chat live via WebSocket
        // then save it to the DB
        LocalDateTime replyTime = LocalDateTime.now();
        MessageDTO replyDto = new MessageDTO();
        replyDto.setStudyGroupId(request.getGroupId());
        replyDto.setSenderName("AI Assistant");
        replyDto.setContent(aiResponse.getReply());
        replyDto.setSentAt(replyTime);
        messagingTemplate.convertAndSend(topic, replyDto);
        chatService.saveSystemMessage(request.getGroupId(), "AI Assistant", aiResponse.getReply(), replyTime);

        // Return the reply to the frontend that made the POST /ai/chat request
        return aiResponse;
    }
}
