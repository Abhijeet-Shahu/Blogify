package com.bloggingapp.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloggingapp.backend.service.AiService;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateBlog(@RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String category = request.get("category");
            
            String generatedText = aiService.generateContent(title, category);

            return ResponseEntity.ok(Map.of("content", generatedText));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generate-image")
    public ResponseEntity<Map<String, String>> generateImage(@RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String category = request.get("category");

            String imageUrl = aiService.generateImage(title, category);

            if (imageUrl == null || imageUrl.startsWith("Error")) {
                return ResponseEntity.status(500).body(Map.of("error", "Image generation failed."));
            }

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}