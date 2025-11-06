package com.bloggingapp.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bloggingapp.backend.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find comments by blog ID
    List<Comment> findByBlog_Id(Long blogId);
    
    // Find comments by blog ID with pagination
    Page<Comment> findByBlog_Id(Long blogId, Pageable pageable);
    
    // Find comments by blog ID ordered by creation date
    List<Comment> findByBlog_IdOrderByCreatedAtDesc(Long blogId);
    
    // Find comments by blog ID with pagination and ordering
    Page<Comment> findByBlog_IdOrderByCreatedAtDesc(Long blogId, Pageable pageable);
    
    // Find comments by author ID
    List<Comment> findByAuthor_Id(Long authorId);
    
    // Find comments by author ID with pagination
    Page<Comment> findByAuthor_Id(Long authorId, Pageable pageable);
    
    // Find comments by blog and author (for authorization)
    List<Comment> findByBlog_IdAndAuthor_Email(Long blogId, String authorEmail);
    
    // Count comments by blog ID
    Long countByBlog_Id(Long blogId);
    
    // Count comments by author ID
    Long countByAuthor_Id(Long authorId);
    
    // Find recent comments across all blogs
    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Find comments with blog information (for dashboard)
    @Query("SELECT c FROM Comment c JOIN FETCH c.blog WHERE c.author.id = :authorId ORDER BY c.createdAt DESC")
    List<Comment> findCommentsWithBlogByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
    
    // Delete comments by blog ID (for cascade delete)
    void deleteByBlog_Id(Long blogId);
    
    // Delete comments by author ID
    void deleteByAuthor_Id(Long authorId);
}