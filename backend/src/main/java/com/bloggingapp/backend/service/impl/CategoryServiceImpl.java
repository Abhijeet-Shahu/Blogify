package com.bloggingapp.backend.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bloggingapp.backend.model.Category;
import com.bloggingapp.backend.repository.CategoryRepository;
import com.bloggingapp.backend.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category createCategory(Category category) {
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new RuntimeException("Category already exists: " + category.getName());
        }
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name);
    }

    public boolean categoryExists(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    public List<Category> getPopularCategories(Pageable pageable) {
        return categoryRepository.findPopularCategories(pageable);
    }

    public long getCategoryCount() {
        return categoryRepository.count();
    }
}