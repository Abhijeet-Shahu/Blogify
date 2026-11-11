package com.bloggingapp.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloggingapp.backend.dto.AiGenerationRequest;
import com.bloggingapp.backend.service.AiService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateBlog(@Valid @RequestBody AiGenerationRequest request) {
        try {
            String generatedText = aiService.generateContent(request.getTitle().trim(), request.getCategory().trim());

            return ResponseEntity.ok(Map.of("content", generatedText));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Content generation failed"));
        }
    }

    @PostMapping("/generate-image")
    public ResponseEntity<Map<String, String>> generateImage(@Valid @RequestBody AiGenerationRequest request) {
        try {
            String imageUrl = aiService.generateImage(request.getTitle().trim(), request.getCategory().trim());

            if (imageUrl == null || imageUrl.startsWith("Error")) {
                return ResponseEntity.status(500).body(Map.of("error", "Image generation failed."));
            }

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Image generation failed"));
        }
    }
}