package com.bloggingapp.backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bloggingapp.backend.dto.BlogRequest;
import com.bloggingapp.backend.model.Blog;
import com.bloggingapp.backend.model.Category;
import com.bloggingapp.backend.service.BlogService;
import com.bloggingapp.backend.service.CategoryService;
import com.bloggingapp.backend.service.UserService;

@Controller
@RequestMapping("/blog")
public class BlogController {

    private final BlogService blogService;
    private final CategoryService categoryService;
    private final UserService userService;

    public BlogController(BlogService blogService, CategoryService categoryService, UserService userService) {
        this.blogService = blogService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    // Show create blog page - FIXED: Added categories to model
    @GetMapping("/createBlog")
    public String showCreateBlogPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        // Get categories from service
        List<Category> categories = categoryService.getAllCategories();
        System.out.println("Categories found: " + categories.size()); // Debug log
        
        // Add categories to model
        model.addAttribute("categories", categories);
        return "createBlog";
    }

    // Process blog creation - UPDATED: Redirect to categories page with specific category
    @PostMapping("/save")
    public String createBlog(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long categoryId,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String excerpt,
            @RequestParam(required = false) String status,
            @RequestParam(value = "featuredImage", required = false) MultipartFile featuredImage,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (principal == null) {
                return "redirect:/login";
            }
            
            // Create BlogRequest manually
            BlogRequest blogRequest = new BlogRequest();
            blogRequest.setTitle(title);
            blogRequest.setContent(content);
            blogRequest.setCategoryId(categoryId);
            blogRequest.setTags(tags);
            blogRequest.setExcerpt(excerpt);
            blogRequest.setStatus(status != null ? status : "DRAFT");
            
            Blog blog = blogService.createBlog(blogRequest, principal.getName(), featuredImage);
            
            // Get category for the redirect
            Category category = categoryService.getCategoryById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            redirectAttributes.addFlashAttribute("success", "Blog post '" + blog.getTitle() + "' saved successfully!");
            
            // Redirect to categories page with the specific category slug
            return "redirect:/categories?highlight=" + category.getSlug();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            
            // Add categories again when there's an error to maintain the form state
            List<Category> categories = categoryService.getAllCategories();
            redirectAttributes.addFlashAttribute("categories", categories);
            
            return "redirect:/blog/createBlog";
        }
    }

    // Show single blog post
    @GetMapping("/{id}")
    public String showBlogPost(@PathVariable Long id, Model model, Principal principal) {
        Optional<Blog> blog = blogService.getBlogById(id);
        if (blog.isEmpty()) {
            return "redirect:/";
        }
        
        // Increment view count
        blogService.incrementViewCount(id);
        
        model.addAttribute("post", blog.get());
        
        // Add authentication status for the login prompt
        boolean isAuthenticated = principal != null;
        model.addAttribute("isAuthenticated", isAuthenticated);
        
        return "ReadPage";
    }

    // Show blog posts by category
    @GetMapping("/category/{categoryId}")
    public String showBlogsByCategory(@PathVariable Long categoryId,
                                     @RequestParam(defaultValue = "0") int page,
                                     Model model) {
        Pageable pageable = PageRequest.of(page, 9, Sort.by("createdAt").descending());
        Page<Blog> blogPage = blogService.getBlogsByCategory(categoryId, pageable);
        Optional<Category> category = categoryService.getCategoryById(categoryId);

        model.addAttribute("posts", blogPage.getContent());
        model.addAttribute("category", category.orElse(null));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());
        model.addAttribute("totalPosts", blogPage.getTotalElements());
        model.addAttribute("categories", categoryService.getAllCategories());

        return "section";
    }

    // Show all blogs with pagination
    @GetMapping("/all")
    public String showAllBlogs(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String search,
                              Model model) {
        Pageable pageable = PageRequest.of(page, 9, Sort.by("createdAt").descending());
        Page<Blog> blogPage;

        if (search != null && !search.trim().isEmpty()) {
            blogPage = blogService.searchBlogs(search, pageable);
            model.addAttribute("searchQuery", search);
        } else {
            blogPage = blogService.getAllBlogs(pageable);
        }

        model.addAttribute("posts", blogPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());
        model.addAttribute("totalPosts", blogPage.getTotalElements());
        model.addAttribute("categories", categoryService.getAllCategories());

        return "section";
    }

    // Delete blog post
    @PostMapping("/{id}/delete")
    public String deleteBlog(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            blogService.deleteBlog(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Blog post deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
}