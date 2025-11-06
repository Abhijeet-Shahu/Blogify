package com.bloggingapp.backend.service;

import com.bloggingapp.backend.model.Comment;
import java.util.List;

public interface CommentService {
    Comment addComment(Comment comment);
    Comment addComment(Comment comment, String authorEmail);
    Comment addComment(Comment comment, String authorEmail, Long blogId); // ADD THIS METHOD
    List<Comment> getCommentsByBlog(Long blogId);
    void deleteComment(Long id, String authorEmail);
}