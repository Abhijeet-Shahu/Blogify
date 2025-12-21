package com.bloggingapp.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.bloggingapp.backend.dto.BlogRequest;
import com.bloggingapp.backend.model.Blog;
import com.bloggingapp.backend.model.BlogStatus;

public interface BlogService {

    // Basic CRUD operations
    Blog createBlog(Blog blog);
    Blog createBlog(BlogRequest request, String authorEmail, MultipartFile featuredImage);
    List<Blog> getAllBlogs();
    Page<Blog> getAllBlogs(Pageable pageable);
    Optional<Blog> getBlogById(Long id);
    void deleteBlog(Long id);
    void deleteBlog(Long id, String authorEmail);
    Blog updateBlog(Long id, BlogRequest request, MultipartFile featuredImage, String authorEmail);
    
    // Blog retrieval by various criteria
    List<Blog> getBlogsByUser(Long userId);
    Page<Blog> getBlogsByUser(Long userId, Pageable pageable);
    List<Blog> getBlogsByCategory(Long categoryId);
    Page<Blog> getBlogsByCategory(Long categoryId, Pageable pageable);
    Page<Blog> getBlogsByCategoryAndStatus(Long categoryId, BlogStatus status, Pageable pageable);
    Page<Blog> getBlogsByStatus(BlogStatus status, Pageable pageable);
    
    // Search functionality
    Page<Blog> searchBlogs(String keyword, Pageable pageable);
    Page<Blog> searchPublishedBlogs(String keyword, Pageable pageable);
    
    // Special blog collections
    Page<Blog> getFeaturedBlogs(Pageable pageable);
    Page<Blog> getPublishedBlogs(Pageable pageable);
    Page<Blog> getPopularBlogs(Pageable pageable);
    List<Blog> getPopularPosts(int limit);
    
    // Statistics and counts
    Long getTotalPostCount();
    Long getPostCountByCategory(Long categoryId);
    Long getPostCountByUser(Long userId);
    
    // Blog operations
    void incrementViewCount(Long blogId);
    Blog updateBlogStatus(Long id, BlogStatus status, String authorEmail);
    
    // Additional methods from your implementation
    Page<Blog> getBlogsByUserAndStatus(Long userId, BlogStatus status, Pageable pageable);
    Blog getBlogByIdWithViewCount(Long id);
}