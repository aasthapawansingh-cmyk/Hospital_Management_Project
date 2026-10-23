package com.example.hosptial;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
public String draftVisitSummary(String clinicianNotes) {
    if (apiKey == null || apiKey.isBlank()) {
        throw new IllegalStateException("GROQ_API_KEY is not configured");
    }

    String instructions = """
            Create a short, clear summary using only the clinician notes provided.
            Do not add facts that are not in the notes.
            Do not diagnose, recommend treatment, prescribe, or make decisions.
            If information is missing, say it was not provided.
            This is an unapproved draft for a clinician to review.
            """;

    List<Map<String, Object>> messages = List.of(
            Map.of("role", "system", "content", instructions),
            Map.of("role", "user", "content", clinicianNotes)
    );

    Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", messages
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    HttpEntity<Map<String, Object>> request =
            new HttpEntity<>(requestBody, headers);

    Map<String, Object> response = restTemplate.postForObject(
            apiUrl,
            request,
            Map.class
    );

    if (response == null || !(response.get("choices") instanceof List<?> choices)
            || choices.isEmpty()
            || !(choices.get(0) instanceof Map<?, ?> firstChoice)
            || !(firstChoice.get("message") instanceof Map<?, ?> message)
            || !(message.get("content") instanceof String content)) {
        throw new IllegalStateException("The AI provider returned an unexpected response");
    }

    return content.trim();
}
}