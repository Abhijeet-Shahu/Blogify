package com.bloggingapp.backend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bloggingapp.backend.model.Comment;
import com.bloggingapp.backend.service.CommentService;
import com.bloggingapp.backend.service.UserService;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(@RequestBody Comment comment, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        Comment savedComment = commentService.addComment(comment, principal.getName());
        return ResponseEntity.ok(savedComment);
    }

    @GetMapping("/blog/{blogId}")
    public List<Comment> getCommentsByBlog(@PathVariable Long blogId) {
        return commentService.getCommentsByBlog(blogId);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, Principal principal) {
        commentService.deleteComment(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}