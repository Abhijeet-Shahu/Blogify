package com.bloggingapp.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bloggingapp.backend.model.Blog;
import com.bloggingapp.backend.model.BlogStatus;

public interface BlogRepository extends JpaRepository<Blog, Long> {

    // Find blogs by author ID
    List<Blog> findByAuthor_Id(Long userId);
    
    // Find blogs by author ID with pagination
    Page<Blog> findByAuthor_Id(Long userId, Pageable pageable);
    
    // Find blogs by category ID
    List<Blog> findByCategory_Id(Long categoryId);
    
    // Find blogs by category ID with pagination
    Page<Blog> findByCategory_Id(Long categoryId, Pageable pageable);
    
    // Find blogs by status
    List<Blog> findByStatus(BlogStatus status);
    
    // Find blogs by status with pagination
    Page<Blog> findByStatus(BlogStatus status, Pageable pageable);
    
    // Find blogs by author and status
    List<Blog> findByAuthor_IdAndStatus(Long userId, BlogStatus status);
    
    // Find blogs by category and status
    Page<Blog> findByCategory_IdAndStatus(Long categoryId, BlogStatus status, Pageable pageable);

    // Search blogs by title (case-insensitive)
    List<Blog> findByTitleContainingIgnoreCase(String keyword);
    
    // Search blogs by title with pagination
    Page<Blog> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    
    // Search blogs by title or content
    @Query("SELECT b FROM Blog b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Blog> findByTitleOrContentContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);
    
    // Search published blogs by title or content
    @Query("SELECT b FROM Blog b WHERE b.status = 'PUBLISHED' AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Blog> searchPublishedBlogs(@Param("keyword") String keyword, Pageable pageable);

    // Find featured/popular blogs (you can customize the criteria)
    @Query("SELECT b FROM Blog b WHERE b.status = 'PUBLISHED' ORDER BY b.viewCount DESC, b.createdAt DESC")
    Page<Blog> findFeaturedBlogs(Pageable pageable);
    
    // Find recent blogs
    Page<Blog> findByStatusOrderByCreatedAtDesc(BlogStatus status, Pageable pageable);
    
    // Count blogs by category
    @Query("SELECT COUNT(b) FROM Blog b WHERE b.category.id = :categoryId AND b.status = 'PUBLISHED'")
    Long countByCategoryIdAndPublished(@Param("categoryId") Long categoryId);
    
    // Count blogs by author
    Long countByAuthor_Id(Long authorId);
    
    // Count published blogs by author
    Long countByAuthor_IdAndStatus(Long authorId, BlogStatus status);
    
    // Find blog by ID and author (for authorization)
    Optional<Blog> findByIdAndAuthor_Email(Long id, String authorEmail);
    
    // Increment view count - FIXED: Added @Modifying and @Transactional
    @Modifying
    @Transactional
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.id = :blogId")
    void incrementViewCount(@Param("blogId") Long blogId);
    
    // Find blogs with most comments
    @Query("SELECT b FROM Blog b WHERE b.status = 'PUBLISHED' ORDER BY SIZE(b.comments) DESC")
    Page<Blog> findPopularBlogs(Pageable pageable);
    
    // Find published blogs by author
    Page<Blog> findByAuthor_IdAndStatus(Long userId, BlogStatus status, Pageable pageable);
    
    // ADD THIS METHOD - Eagerly load blog with all relationships
    @Query("SELECT b FROM Blog b " +
           "LEFT JOIN FETCH b.author " +
           "LEFT JOIN FETCH b.category " +
           "LEFT JOIN FETCH b.comments c " +
           "LEFT JOIN FETCH c.author " +
           "WHERE b.id = :id")
    Optional<Blog> findByIdWithRelations(@Param("id") Long id);
}