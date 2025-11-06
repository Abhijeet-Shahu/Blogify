package com.bloggingapp.backend.service;

public interface AiService {
    String generateContent(String title, String category);
    String generateImage(String title, String category);
}
