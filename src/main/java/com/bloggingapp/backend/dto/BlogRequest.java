package com.bloggingapp.backend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class BlogRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Content must be at most 10000 characters")
    private String content;

    @Size(max = 500, message = "Excerpt must be at most 500 characters")
    private String excerpt;

    @Size(max = 500, message = "Tags must be at most 500 characters")
    private String tags;

    private String status;

    private MultipartFile featuredImage; // Change from imageUrl to featuredImage

    @NotNull(message = "Category is required")
    private Long categoryId;
}