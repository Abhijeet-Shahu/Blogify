package com.bloggingapp.backend.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.bloggingapp.backend.service.AiService;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

@Service
public class AiServiceImpl implements AiService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${stability.api.key:}")
    private String stabilityApiKey;

    private static final String IMAGE_SAVE_PATH = "src/main/resources/static/assets/generated-assets/";
    private static final String STABILITY_URL = "https://api.stability.ai/v1/generation/stable-diffusion-v1-5/text-to-image";   
    
    // CORRECT Gemini API URLs - try these in order
    private static final String[] GEMINI_URLS = {
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent"
    };

    private final RestTemplate restTemplate;

    public AiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String generateContent(String title, String category) {
        // Check if API key is configured
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            System.out.println("❌ Gemini API key not configured");
            return generateFallbackContent(title, category);
        }

        System.out.println("🔑 API Key: " + (geminiApiKey.length() > 8 ? 
            geminiApiKey.substring(0, 8) + "..." : "Invalid"));
        
        // Try different Gemini API endpoints
        for (String geminiUrl : GEMINI_URLS) {
            try {
                String result = tryGeminiEndpoint(geminiUrl, title, category);
                if (result != null && !result.startsWith("Error")) {
                    return result;
                }
            } catch (Exception e) {
                System.err.println("❌ Failed with URL " + geminiUrl + ": " + e.getMessage());
            }
        }
        
        // All endpoints failed
        return generateFallbackContent(title, category);
    }

    private String tryGeminiEndpoint(String geminiUrl, String title, String category) {
        try {
            String prompt = createPrompt(title, category);
            
            // Build the request body according to Gemini API documentation
            Map<String, Object> requestBody = new HashMap<>();
            
            // Contents array
            Map<String, Object> contentItem = new HashMap<>();
            Map<String, Object> partsItem = new HashMap<>();
            partsItem.put("text", prompt);
            
            contentItem.put("parts", List.of(partsItem));
            requestBody.put("contents", List.of(contentItem));
            
            // Generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 2048);
            requestBody.put("generationConfig", generationConfig);

            // Safety settings (optional but recommended)
            Map<String, Object> safetySettings = Map.of(
                "category", "HARM_CATEGORY_DANGEROUS_CONTENT",
                "threshold", "BLOCK_MEDIUM_AND_ABOVE"
            );
            requestBody.put("safetySettings", List.of(safetySettings));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String fullUrl = geminiUrl + "?key=" + geminiApiKey;
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("🚀 Calling Gemini API: " + geminiUrl);
            System.out.println("📝 Request body: " + requestBody);

            ResponseEntity<Map> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );

            System.out.println("📡 Response status: " + response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("❌ API returned status: " + response.getStatusCode());
                if (response.getBody() != null) {
                    System.err.println("❌ Response body: " + response.getBody());
                }
                return null;
            }

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                System.err.println("❌ Null response body");
                return null;
            }

            System.out.println("✅ Raw response: " + responseBody);

            // Extract content from response
            String generatedContent = extractContentFromGeminiResponse(responseBody);
            if (generatedContent != null && !generatedContent.trim().isEmpty()) {
                System.out.println("🎉 Successfully generated AI content!");
                return generatedContent;
            } else {
                System.err.println("❌ No content extracted from response");
                return null;
            }

        } catch (Exception e) {
            System.err.println("💥 Exception with endpoint " + geminiUrl + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String extractContentFromGeminiResponse(Map<String, Object> responseBody) {
        try {
            // Debug: Print the full response structure
            System.out.println("🔍 Response keys: " + responseBody.keySet());
            
            if (responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                System.out.println("🔍 Number of candidates: " + candidates.size());
                
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    System.out.println("🔍 Candidate keys: " + firstCandidate.keySet());
                    
                    if (firstCandidate.containsKey("content")) {
                        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                        System.out.println("🔍 Content keys: " + content.keySet());
                        
                        if (content.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            System.out.println("🔍 Number of parts: " + parts.size());
                            
                            if (!parts.isEmpty()) {
                                Map<String, Object> firstPart = parts.get(0);
                                System.out.println("🔍 Part keys: " + firstPart.keySet());
                                
                                String text = (String) firstPart.get("text");
                                if (text != null && !text.trim().isEmpty()) {
                                    System.out.println("✅ Extracted text length: " + text.length());
                                    return text;
                                }
                            }
                        }
                    }
                }
            }
            
            // Alternative extraction method
            if (responseBody.containsKey("promptFeedback")) {
                System.out.println("⚠️ Prompt feedback found, but no content");
            }
            
            System.err.println("❌ Could not extract content from response structure");
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ Error extracting content: " + e.getMessage());
            return null;
        }
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
        // Use placeholder service
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
               "*Note: To enable AI content generation, please ensure your Gemini API key is properly configured in application.properties*";
    }
}