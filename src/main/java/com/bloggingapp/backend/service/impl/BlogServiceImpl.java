package com.bloggingapp.backend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bloggingapp.backend.dto.BlogRequest;
import com.bloggingapp.backend.model.Blog;
import com.bloggingapp.backend.model.BlogStatus;
import com.bloggingapp.backend.model.Category;
import com.bloggingapp.backend.model.User;
import com.bloggingapp.backend.repository.BlogRepository;
import com.bloggingapp.backend.repository.CategoryRepository;
import com.bloggingapp.backend.repository.UserRepository;
import com.bloggingapp.backend.service.BlogService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/blogs/";

    public BlogServiceImpl(BlogRepository blogRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Blog createBlog(Blog blog) {
        return blogRepository.save(blog);
    }

    @Override
    public Blog createBlog(BlogRequest request, String authorEmail, MultipartFile featuredImage) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setExcerpt(request.getExcerpt());
        blog.setTags(request.getTags());
        blog.setAuthor(author);
        blog.setCategory(category);
        blog.setStatus(BlogStatus.valueOf(request.getStatus()));

        // Handle featured image upload
        if (featuredImage != null && !featuredImage.isEmpty()) {
            String imageUrl = saveImage(featuredImage);
            blog.setFeaturedImage(imageUrl);
        }

        return blogRepository.save(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> getAllBlogs(Pageable pageable) {
        return blogRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Optional<Blog> getBlogById(Long id) {
        Optional<Blog> blog = blogRepository.findById(id);
        blog.ifPresent(b -> {
            // Increment view count using repository method
            blogRepository.incrementViewCount(b.getId());
            // Refresh the entity to get updated view count
            entityManager.refresh(b);
        });
        return blog;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Blog> getBlogsByUser(Long userId) {
        return blogRepository.findByAuthor_Id(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> getBlogsByUser(Long userId, Pageable pageable) {
        return blogRepository.findByAuthor_Id(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Blog> getBlogsByCategory(Long categoryId) {
        return blogRepository.findByCategory_Id(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> getBlogsByCategory(Long categoryId, Pageable pageable) {
        return blogRepository.findByCategory_Id(categoryId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> getBlogsByCategoryAndStatus(Long categoryId, BlogStatus status, Pageable pageable) {
        return blogRepository.findByCategory_IdAndStatus(categoryId, status, pageable);
    }

    @Override
    @Transactional
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteBlog(Long id, String authorEmail) {
        Blog blog = blogRepository.findByIdAndAuthor_Email(id, authorEmail)
                .orElseThrow(() -> new RuntimeException("Blog not found or unauthorized"));
        blogRepository.delete(blog);
    }

    @Override
    @Transactional
    public Blog updateBlog(Long id, BlogRequest request, MultipartFile featuredImage, String authorEmail) {
        Blog blog = blogRepository.findByIdAndAuthor_Email(id, authorEmail)
                .orElseThrow(() -> new RuntimeException("Blog not found or unauthorized"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setExcerpt(request.getExcerpt());
        blog.setTags(request.getTags());
        blog.setCategory(category);
        blog.setStatus(BlogStatus.valueOf(request.getStatus()));

        if (featuredImage != null && !featuredImage.isEmpty()) {
            String imageUrl = saveImage(featuredImage);
            blog.setFeaturedImage(imageUrl);
        }

        return blogRepository.save(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> searchBlogs(String keyword, Pageable pageable) {
        return blogRepository.findByTitleOrContentContainingIgnoreCase(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> searchPublishedBlogs(String keyword, Pageable pageable) {
        return blogRepository.searchPublishedBlogs(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> getFeaturedBlogs(Pageable pageable) {
        return blogRepository.findFeaturedBlogs(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> getPublishedBlogs(Pageable pageable) {
        return blogRepository.findByStatusOrderByCreatedAtDesc(BlogStatus.PUBLISHED, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> getBlogsByStatus(BlogStatus status, Pageable pageable) {
        return blogRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> getPopularBlogs(Pageable pageable) {
        return blogRepository.findPopularBlogs(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalPostCount() {
        return blogRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPostCountByCategory(Long categoryId) {
        return blogRepository.countByCategoryIdAndPublished(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPostCountByUser(Long userId) {
        return blogRepository.countByAuthor_IdAndStatus(userId, BlogStatus.PUBLISHED);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long blogId) {
        blogRepository.incrementViewCount(blogId);
    }

    @Override
    @Transactional
    public Blog updateBlogStatus(Long id, BlogStatus status, String authorEmail) {
        Blog blog = blogRepository.findByIdAndAuthor_Email(id, authorEmail)
                .orElseThrow(() -> new RuntimeException("Blog not found or unauthorized"));
        blog.setStatus(status);
        return blogRepository.save(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Blog> getPopularPosts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("viewCount").descending());
        return blogRepository.findByStatus(BlogStatus.PUBLISHED, pageable).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Blog> getBlogsByUserAndStatus(Long userId, BlogStatus status, Pageable pageable) {
        return blogRepository.findByAuthor_IdAndStatus(userId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Blog getBlogByIdWithViewCount(Long id) {
        // Use the new method that eagerly loads relationships
        Blog blog = blogRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + id));
        
        // Debug logging
        System.out.println("=== BLOG LOADED WITH RELATIONS ===");
        System.out.println("Blog Title: " + blog.getTitle());
        System.out.println("Author: " + (blog.getAuthor() != null ? blog.getAuthor().getUsername() : "NULL"));
        System.out.println("Category: " + (blog.getCategory() != null ? blog.getCategory().getName() : "NULL"));
        System.out.println("Comments Count: " + (blog.getComments() != null ? blog.getComments().size() : "NULL"));
        
        // Increment view count using repository method
        blogRepository.incrementViewCount(id);
        
        return blog;
    }

    private String saveImage(MultipartFile image) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, image.getBytes());
            return "/uploads/blogs/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage());
        }
    }
}