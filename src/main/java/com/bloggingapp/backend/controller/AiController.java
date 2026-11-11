package com.bloggingapp.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bloggingapp.backend.dto.AiGenerationRequest;
import com.bloggingapp.backend.service.AiService;
import com.bloggingapp.backend.service.AiRateLimiter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final AiRateLimiter rateLimiter;

    public AiController(AiService aiService, AiRateLimiter rateLimiter) {
        this.aiService = aiService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateBlog(
            @Valid @RequestBody AiGenerationRequest request, Principal principal,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (!rateLimiter.allow(rateLimitKey(principal, httpRequest))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "AI request limit exceeded"));
        }
        try {
            String generatedText = aiService.generateContent(request.getTitle().trim(), request.getCategory().trim());

            return ResponseEntity.ok(Map.of("content", generatedText));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Content generation failed"));
        }
    }

    @PostMapping("/generate-image")
    public ResponseEntity<Map<String, String>> generateImage(
            @Valid @RequestBody AiGenerationRequest request, Principal principal,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (!rateLimiter.allow(rateLimitKey(principal, httpRequest))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "AI request limit exceeded"));
        }
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

    private String rateLimitKey(Principal principal, jakarta.servlet.http.HttpServletRequest request) {
        return principal != null ? "user:" + principal.getName() : "ip:" + request.getRemoteAddr();
    }
}