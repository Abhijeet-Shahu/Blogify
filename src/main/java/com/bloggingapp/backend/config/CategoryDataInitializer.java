package com.bloggingapp.backend.config;

import com.bloggingapp.backend.model.Category;
import com.bloggingapp.backend.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CategoryDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategoryDataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if categories already exist
        long categoryCount = categoryRepository.count();
        
        if (categoryCount == 0) {
            System.out.println("🚀 Initializing default categories...");
            createDefaultCategories();
            System.out.println("✅ Default categories created successfully!");
        } else {
            System.out.println("📁 Categories already exist: " + categoryCount + " categories found");
        }
    }

    private void createDefaultCategories() {
        List<Category> defaultCategories = Arrays.asList(
            createCategory("Technology", "Latest technology trends and innovations", "technology"),
            createCategory("Programming", "Coding tutorials and programming concepts", "programming"),
            createCategory("Web Development", "Frontend and backend web development", "web-development"),
            createCategory("Mobile Development", "iOS and Android app development", "mobile-development"),
            createCategory("Data Science", "AI, Machine Learning and Data Analysis", "data-science"),
            createCategory("DevOps", "Infrastructure, deployment and operations", "devops"),
            createCategory("Cyber Security", "Security practices and threat prevention", "cyber-security"),
            createCategory("Cloud Computing", "Cloud services and infrastructure", "cloud-computing"),
            createCategory("Blockchain", "Cryptocurrency and blockchain technology", "blockchain"),
            createCategory("UI/UX Design", "User interface and experience design", "ui-ux-design"),
            createCategory("Software Engineering", "Software development methodologies", "software-engineering"),
            createCategory("Career Advice", "Tech career guidance and tips", "career-advice"),
            createCategory("Productivity", "Tools and tips for better productivity", "productivity"),
            createCategory("Startups", "Startup stories and entrepreneurship", "startups"),
            createCategory("Open Source", "Open source projects and contributions", "open-source")
        );

        categoryRepository.saveAll(defaultCategories);
    }

    private Category createCategory(String name, String description, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setSlug(slug);
        return category;
    }
}