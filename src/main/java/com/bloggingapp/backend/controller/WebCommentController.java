package com.bloggingapp.backend.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bloggingapp.backend.model.Comment;
import com.bloggingapp.backend.service.CommentService;
import com.bloggingapp.backend.service.UserService;
import com.bloggingapp.backend.service.BlogService;

@Controller
public class WebCommentController {

    private final CommentService commentService;
    private final UserService userService;
    private final BlogService blogService;

    public WebCommentController(CommentService commentService, UserService userService, BlogService blogService) {
        this.commentService = commentService;
        this.userService = userService;
        this.blogService = blogService;
    }

    @PostMapping("/posts/{id}/comments")
    public String addComment(@PathVariable Long id,
                           @RequestParam String content,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            if (principal == null) {
                return "redirect:/login";
            }
            
            // Create and save the comment
            Comment comment = new Comment();
            comment.setContent(content);
            
            // Use the service method that handles author and blog association
            commentService.addComment(comment, principal.getName(), id);
            
            redirectAttributes.addFlashAttribute("success", "Comment added successfully!");
            return "redirect:/blog/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add comment: " + e.getMessage());
            return "redirect:/blog/" + id;
        }
    }
}