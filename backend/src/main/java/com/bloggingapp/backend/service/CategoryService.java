package com.bloggingapp.backend.service;

import java.util.List;
import java.util.Optional;

import com.bloggingapp.backend.model.Category;

public interface CategoryService {

    Category createCategory(Category category);
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Long id);
    
}
