package com.bloggingapp.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bloggingapp.backend.model.User;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Find user by username (if you have username field)
    Optional<User> findByUsername(String username);
    
    // Find user by email or username
    Optional<User> findByEmailOrUsername(String email, String username);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Find users by role
    Page<User> findByRole(String role, Pageable pageable);
    
    // Find active users
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    Page<User> findActiveUsers(Pageable pageable);
    
    // Count total users
    long count();
    
    // Count users by role
    long countByRole(String role);
    
    // Count active users
    long countByEnabledTrue();
    
    // Find popular writers (users with most published blogs)
    @Query("SELECT u, COUNT(b) as blogCount FROM User u LEFT JOIN Blog b ON b.author = u AND b.status = 'PUBLISHED' GROUP BY u ORDER BY blogCount DESC")
    Page<Object[]> findPopularWriters(Pageable pageable);
    
    // Search users by name or email
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    // Find users with blog count
    @Query("SELECT u, COUNT(b) FROM User u LEFT JOIN Blog b ON b.author = u AND b.status = 'PUBLISHED' GROUP BY u")
    Page<Object[]> findUsersWithBlogCount(Pageable pageable);
    
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.email = :email")
    void updateLastLogin(@Param("email") String email);
    
    // Find users who have published at least one blog
    @Query("SELECT DISTINCT u FROM User u JOIN Blog b ON b.author = u WHERE b.status = 'PUBLISHED'")
    Page<User> findWritersWithPublishedBlogs(Pageable pageable);
    
    // Count writers (users with published blogs)
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN Blog b ON b.author = u WHERE b.status = 'PUBLISHED'")
    Long countWriters();
}