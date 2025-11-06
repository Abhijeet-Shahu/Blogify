package com.bloggingapp.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bloggingapp.backend.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find category by name
    Optional<Category> findByName(String name);
    
    // Find category by name ignoring case
    Optional<Category> findByNameIgnoreCase(String name);
    
    // Check if category exists by name
    boolean existsByName(String name);
    
    // Check if category exists by name ignoring case
    boolean existsByNameIgnoreCase(String name);
    
    // Count total categories
    long count();
    
    // Find categories with blog count
    @Query("SELECT c, COUNT(b) FROM Category c LEFT JOIN Blog b ON b.category = c AND b.status = 'PUBLISHED' GROUP BY c")
    Object[] findCategoriesWithBlogCount();
    
    // Find popular categories (with most blogs)
    @Query("SELECT c FROM Category c LEFT JOIN Blog b ON b.category = c AND b.status = 'PUBLISHED' GROUP BY c ORDER BY COUNT(b) DESC")
    java.util.List<Category> findPopularCategories(org.springframework.data.domain.Pageable pageable);
    
    // Find category by slug (if you implement slugs)
    Optional<Category> findBySlug(String slug);
}