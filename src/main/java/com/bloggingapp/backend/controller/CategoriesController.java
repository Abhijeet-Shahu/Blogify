package com.bloggingapp.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bloggingapp.backend.model.Category;
import com.bloggingapp.backend.service.BlogService;
import com.bloggingapp.backend.service.CategoryService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CategoriesController {

    private final CategoryService categoryService;
    private final BlogService blogService;

    public CategoriesController(CategoryService categoryService, BlogService blogService) {
        this.categoryService = categoryService;
        this.blogService = blogService;
    }

    @GetMapping("/categories")
    public String showCategoriesPage(@RequestParam(required = false) String highlight, Model model) {
        try {
            // Get all categories
            List<Category> allCategories = categoryService.getAllCategories();
            model.addAttribute("categoriesList", allCategories);
            
            // Create a map of category slugs to IDs for counting
            Map<String, Long> slugToIdMap = new HashMap<>();
            for (Category category : allCategories) {
                slugToIdMap.put(category.getSlug(), category.getId());
            }
            
            // Add category counts for the main categories shown in the UI
            model.addAttribute("technologyPostCount", getPostCountForSlug(slugToIdMap, "technology"));
            model.addAttribute("designPostCount", getPostCountForSlug(slugToIdMap, "ui-ux-design"));
            model.addAttribute("businessPostCount", getPostCountForSlug(slugToIdMap, "startups"));
            model.addAttribute("lifestylePostCount", getPostCountForSlug(slugToIdMap, "productivity"));
            model.addAttribute("educationPostCount", getPostCountForSlug(slugToIdMap, "programming"));
            
            // Add sample tags for each category
            model.addAttribute("technologyTags", Arrays.asList("Web Dev", "AI", "Mobile", "Cloud"));
            model.addAttribute("designTags", Arrays.asList("UI/UX", "Figma", "Prototype", "Wireframe"));
            model.addAttribute("businessTags", Arrays.asList("Startup", "Marketing", "Finance", "Strategy"));
            model.addAttribute("lifestyleTags", Arrays.asList("Health", "Travel", "Wellness", "Productivity"));
            model.addAttribute("educationTags", Arrays.asList("Tutorials", "Coding", "Courses", "Tips"));
            
            // Add popular posts (you'll need to implement this in your service)
            // model.addAttribute("popularPosts", blogService.getPopularPosts());
            
            // Add social media links
            Map<String, String> socialMedia = new HashMap<>();
            socialMedia.put("instagram", "https://www.instagram.com/xx_.goku_xx/");
            socialMedia.put("github", "https://github.com/Abhijeet-Shahu");
            model.addAttribute("socialMedia", socialMedia);

            // Add contact info
            Map<String, String> contactInfo = new HashMap<>();
            contactInfo.put("phone", "797752527");
            contactInfo.put("email", "abhijeetshahu22@gmail.com");
            contactInfo.put("address", "Maharashtra, India");
            model.addAttribute("contactInfo", contactInfo);
            
            // Add the highlighted category for animation
            if (highlight != null) {
                model.addAttribute("highlightedCategory", highlight);
            }
            
            return "categories";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to load categories. Please try again later.");
            return "categories";
        }
    }
    
    private Long getPostCountForSlug(Map<String, Long> slugToIdMap, String slug) {
        try {
            Long categoryId = slugToIdMap.get(slug);
            if (categoryId != null) {
                Long count = blogService.getPostCountByCategory(categoryId);
                return count != null ? count : 0L;
            }
            return 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}
