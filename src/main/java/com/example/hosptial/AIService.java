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

    public String askAI(String question) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", question
        );

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(message)
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(requestBody, headers);

        Map<String, Object> response =
                restTemplate.postForObject(
                        apiUrl,
                        request,
                        Map.class
                );

        List<Map<String, Object>> choices =
                (List<Map<String, Object>>) response.get("choices");

        Map<String, Object> firstChoice = choices.get(0);

        Map<String, Object> responseMessage =
                (Map<String, Object>) firstChoice.get("message");

        return (String) responseMessage.get("content");
    }
}