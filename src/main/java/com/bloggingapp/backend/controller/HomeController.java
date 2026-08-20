package com.bloggingapp.backend.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bloggingapp.backend.service.BlogService;
import com.bloggingapp.backend.service.CategoryService;
import com.bloggingapp.backend.service.UserService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final BlogService blogService;
    private final CategoryService categoryService;
    private final UserService userService;

    public HomeController(BlogService blogService, CategoryService categoryService, UserService userService) {
        this.blogService = blogService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        Pageable featuredLimit = PageRequest.of(0, 6);
        
        model.addAttribute("featuredPosts", blogService.getFeaturedBlogs(featuredLimit));
        model.addAttribute("writerCount", userService.getWriterCount());
        model.addAttribute("postCount", blogService.getTotalPostCount());
        model.addAttribute("readerCount", userService.getReaderCount());
        
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        try {
            // Add stats
            model.addAttribute("stats", Map.of(
                "writersCount", userService.getWriterCount(),
                "postsCount", blogService.getTotalPostCount(),
                "readersCount", userService.getReaderCount()
            ));
            
            // Add individual counts for easy access in template
            model.addAttribute("writerCount", userService.getWriterCount());
            model.addAttribute("postCount", blogService.getTotalPostCount());
            model.addAttribute("readerCount", userService.getReaderCount());
            
            // Add team members
            model.addAttribute("teamMembers", Arrays.asList(
                Map.of(
                    "name", "Abhijeet Shahu",
                    "initials", "AS",
                    "role", "CEO & Founder",
                    "description", "Passionate about democratizing content creation and building communities around shared knowledge.",
                    "instagram", "https://www.instagram.com/xx_.goku_xx/",
                    "linkedin", "https://linkedin.com/in/abhijeet",
                    "github", "https://github.com/Abhijeet-Shahu"
                )
            ));
            
            // Add values
            model.addAttribute("values", Arrays.asList(
                Map.of(
                    "iconClass", "fas fa-lightbulb",
                    "title", "Innovation",
                    "description", "We continuously innovate to provide the best writing and reading experience."
                ),
                Map.of(
                    "iconClass", "fas fa-users",
                    "title", "Community",
                    "description", "We believe in the power of community and collaborative knowledge sharing."
                ),
                Map.of(
                    "iconClass", "fas fa-shield-alt",
                    "title", "Quality",
                    "description", "We're committed to maintaining high standards of content quality and user experience."
                ),
                Map.of(
                    "iconClass", "fas fa-rocket",
                    "title", "Growth",
                    "description", "We help writers grow their audience and readers discover amazing content."
                )
            ));
            
            // Add social media links (for footer)
            Map<String, String> socialMedia = new HashMap<>();
            socialMedia.put("instagram", "https://www.instagram.com/xx_.goku_xx/");
            socialMedia.put("github", "https://github.com/Abhijeet-Shahu");
            model.addAttribute("socialMedia", socialMedia);

            // Add contact info (for footer)
            Map<String, String> contactInfo = new HashMap<>();
            contactInfo.put("phone", "123456789");
            contactInfo.put("email", "abhijeetshahu22@gmail.com");
            contactInfo.put("address", "Maharashtra, India");
            model.addAttribute("contactInfo", contactInfo);
            
            return "about";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/contact")
public String contact(Model model) {
    try {
        // Add contact info
        Map<String, String> contactInfo = new HashMap<>();
        contactInfo.put("phone", "123456789");
        contactInfo.put("email", "abhijeetshahu22@gmail.com");
        contactInfo.put("location", "Maharashtra, India");
        model.addAttribute("contactInfo", contactInfo);
        
        // Add social media links - FIXED: use socialMedia instead of socialLinks
        Map<String, String> socialMedia = new HashMap<>();
        socialMedia.put("instagram", "https://www.instagram.com/xx_.goku_xx/");
        socialMedia.put("github", "https://github.com/Abhijeet-Shahu");
        model.addAttribute("socialMedia", socialMedia); // FIXED: changed from socialLinks to socialMedia

        // Add FAQs
        model.addAttribute("faqs", List.of(
            Map.of("question", "How do I start writing on Blogify?", 
                   "answer", "Simply sign up for a free account, complete your profile, and start writing!"),
            Map.of("question", "Is Blogify free to use?", 
                   "answer", "Yes, Blogify is completely free for both readers and writers."),
            Map.of("question", "Can I monetize my content?", 
                   "answer", "Currently we're working on monetization features for our writers.")
        ));
        
        return "contact";
    } catch (Exception e) {
        e.printStackTrace();
        return "error";
    }
    }
}
