package com.bloggingapp.backend.service.impl;

import com.bloggingapp.backend.service.AiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    @Value("${GROQ_API_KEY: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}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Value("${stability.api.key:sk-4ipeVXj9Ca0VUtSzj6VrEn9wFlTw0QJwW6huxyBlvq5iLpMU}")
    private String stabilityApiKey;

    private static final String IMAGE_SAVE_PATH = "src/main/resources/static/assets/generated-assets/";
    private static final String STABILITY_URL = "https://api.stability.ai/v1/generation/stable-diffusion-v1-5/text-to-image";
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final RestTemplate restTemplate;

    public AiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String generateContent(String title, String category) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            System.out.println("Groq API key not configured");
            return generateFallbackContent(title, category);
        }

        try {
            String result = callGroq(title, category);
            if (result != null && !result.isBlank()) {
                return result;
            }
        } catch (Exception e) {
            System.err.println("Groq content generation failed: " + e.getMessage());
        }

        return generateFallbackContent(title, category);
    }

    private String callGroq(String title, String category) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", groqModel);
        requestBody.put("temperature", 0.7);
        requestBody.put("top_p", 0.95);
        requestBody.put("max_tokens", 2048);
        requestBody.put("messages", List.of(
            Map.of(
                "role", "system",
                "content", "You are a professional blog writer. Return only the blog post content."
            ),
            Map.of(
                "role", "user",
                "content", createPrompt(title, category)
            )
        ));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            GROQ_URL,
            HttpMethod.POST,
            entity,
            Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return null;
        }

        return extractContentFromGroqResponse(response.getBody());
    }

    private String extractContentFromGroqResponse(Map<String, Object> responseBody) {
        Object choicesValue = responseBody.get("choices");
        if (!(choicesValue instanceof List<?> choices) || choices.isEmpty()) {
            return null;
        }

        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choice)) {
            return null;
        }

        Object messageValue = choice.get("message");
        if (!(messageValue instanceof Map<?, ?> message)) {
            return null;
        }

        Object content = message.get("content");
        return content instanceof String text ? text.trim() : null;
    }

    private String createPrompt(String title, String category) {
        return String.format(
            "Write a comprehensive, engaging blog post about \"%s\" in the \"%s\" category. \n\n" +
            "Requirements:\n" +
            "- Write in a professional but accessible tone\n" +
            "- Structure with: Introduction, 3-4 main sections, Conclusion\n" +
            "- Include practical examples and insights\n" +
            "- Make it informative and well-researched\n" +
            "- Use proper markdown formatting with headings\n" +
            "- Target length: 800-1200 words\n\n" +
            "Please provide high-quality, original content that would be valuable for readers interested in %s.",
            title, category, category
        );
    }

    @Override
    public String generateImage(String title, String category) {
        if (stabilityApiKey == null || stabilityApiKey.isEmpty()) {
            System.out.println("Stability AI key not configured, using placeholder");
            return generatePlaceholderImage(title, category);
        }

        try {
            String prompt = String.format(
                "Professional blog illustration for article titled \"%s\" about %s. " +
                "Clean, modern, professional style, suitable for blog header.",
                title, category
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + stabilityApiKey);
            headers.set("Accept", "application/json");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text_prompts", List.of(Map.of("text", prompt)));
            requestBody.put("cfg_scale", 7);
            requestBody.put("height", 512);
            requestBody.put("width", 512);
            requestBody.put("samples", 1);
            requestBody.put("steps", 30);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                STABILITY_URL,
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> resp = response.getBody();
                var artifacts = (List<Map<String, Object>>) resp.get("artifacts");
                if (artifacts != null && !artifacts.isEmpty()) {
                    String base64Image = (String) artifacts.get(0).get("base64");
                    if (base64Image != null) {
                        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                        String fileName = "blog_" + System.currentTimeMillis() + ".png";
                        Path outputPath = Paths.get(IMAGE_SAVE_PATH + fileName);
                        Files.createDirectories(outputPath.getParent());
                        Files.write(outputPath, imageBytes);
                        return "/assets/generated-assets/" + fileName;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating image: " + e.getMessage());
        }

        return generatePlaceholderImage(title, category);
    }

    private String generatePlaceholderImage(String title, String category) {
        String encodedTitle = title.replace(" ", "+");
        return "https://placehold.co/800x400/6366F1/FFFFFF?text=" + encodedTitle;
    }

    private String generateFallbackContent(String title, String category) {
        return "# " + title + "\n\n" +
               "## Introduction\n" +
               "This is a comprehensive guide about " + title + " in the " + category + " category. " +
               "While we aim to provide AI-generated content, our system is currently using a high-quality template.\n\n" +
               "## Key Sections to Develop\n" +
               "### 1. Core Concepts\n" +
               "Discuss the fundamental principles of " + title + " and why they matter in " + category + ".\n\n" +
               "### 2. Practical Applications\n" +
               "Explore real-world uses and case studies related to " + title + ".\n\n" +
               "### 3. Future Trends\n" +
               "Examine emerging developments and what's next for " + title + ".\n\n" +
               "## Conclusion\n" +
               "Summarize the key takeaways and provide actionable insights.\n\n" +
               "*Note: To enable AI content generation, please ensure your Groq API key is properly configured.*";
    }
}
