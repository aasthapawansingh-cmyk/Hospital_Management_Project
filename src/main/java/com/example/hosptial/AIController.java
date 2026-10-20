package com.example.hosptial;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public String askAI(@RequestBody AIRequest request) {
        return aiService.askAI(request.getQuestion());
    }
}