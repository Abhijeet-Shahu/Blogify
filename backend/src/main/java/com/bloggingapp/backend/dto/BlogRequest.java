package com.bloggingapp.backend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BlogRequest {
    private String title;
    private String content;
    private String excerpt;
    private String tags;
    private String status;
    private MultipartFile featuredImage; // Change from imageUrl to featuredImage
    private Long categoryId;
}