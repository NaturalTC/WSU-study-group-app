package com.github.wsustudygroupapp.service;

import com.github.wsustudygroupapp.dto.AiChatRequest;
import com.github.wsustudygroupapp.dto.AiChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// TODO: Jose Jimenez — proxies student messages to the ChatGPT API and returns the AI reply
// chat() → send the student's message to OpenAI and return the reply as AiChatResponse

@Service
public class AiService {

    // TODO: add "openai.api.key" to application.properties and inject it here
    //       IMPORTANT: never hardcode the key — always read from environment/properties
    @Value("${openai.api.key:}")
    private String openAiApiKey;

    // TODO: add "openai.model" to application.properties (e.g. "gpt-4o-mini" for lower cost)
    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    // TODO: inject a RestTemplate or WebClient bean for making HTTP calls to the OpenAI API
    //       Example: @Autowired private RestTemplate restTemplate;

    // ─────────────────────────────────────────────────────────────────
    // chat()
    //
    // ENDPOINT CALLED:  POST https://api.openai.com/v1/chat/completions
    //
    // REQUEST HEADERS:
    //   Authorization: Bearer <openAiApiKey>
    //   Content-Type:  application/json
    //
    // REQUEST BODY (OpenAI format):
    //   {
    //     "model": "gpt-4o-mini",
    //     "messages": [
    //       { "role": "system", "content": "<course-aware system prompt>" },
    //       { "role": "user",   "content": request.getMessage() }
    //     ]
    //   }
    //
    // EXPECTED RESPONSE:
    //   choices[0].message.content → the AI's reply string
    //
    // TODO:
    //   1. Build a system prompt that tells the AI it is a study assistant for the group's course
    //   2. POST the request to the OpenAI API using RestTemplate or WebClient
    //   3. Parse choices[0].message.content from the JSON response
    //   4. Wrap the reply in an AiChatResponse and return it
    //   5. Handle API errors gracefully — return a friendly fallback message on failure
    // ─────────────────────────────────────────────────────────────────
    public AiChatResponse chat(AiChatRequest request) {
        // TODO: implement
        return null;
    }
}
