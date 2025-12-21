package com.bloggingapp.backend.service.impl;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bloggingapp.backend.model.Comment;
import com.bloggingapp.backend.model.User;
import com.bloggingapp.backend.model.Blog;
import com.bloggingapp.backend.repository.CommentRepository;
import com.bloggingapp.backend.repository.UserRepository;
import com.bloggingapp.backend.repository.BlogRepository;
import com.bloggingapp.backend.service.CommentService;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;

    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository, BlogRepository blogRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.blogRepository = blogRepository;
    }

    @Override
    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Comment addComment(Comment comment, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        comment.setAuthor(author);
        return commentRepository.save(comment);
    }

    // ADD THIS NEW METHOD
    @Override
    public Comment addComment(Comment comment, String authorEmail, Long blogId) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        
        comment.setAuthor(author);
        comment.setBlog(blog);
        
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> getCommentsByBlog(Long blogId) {
        return commentRepository.findByBlog_IdOrderByCreatedAtDesc(blogId);
    }

    public Page<Comment> getCommentsByBlog(Long blogId, Pageable pageable) {
        return commentRepository.findByBlog_IdOrderByCreatedAtDesc(blogId, pageable);
    }

    public List<Comment> getCommentsByAuthor(Long authorId) {
        return commentRepository.findByAuthor_Id(authorId);
    }

    public Page<Comment> getCommentsByAuthor(Long authorId, Pageable pageable) {
        return commentRepository.findByAuthor_Id(authorId, pageable);
    }

    @Override
    public void deleteComment(Long commentId, String authorEmail) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!comment.getAuthor().getEmail().equals(authorEmail)) {
            throw new RuntimeException("Unauthorized to delete this comment");
        }
        commentRepository.delete(comment);
    }

    public Long getCommentCountByBlog(Long blogId) {
        return commentRepository.countByBlog_Id(blogId);
    }

    public Long getCommentCountByAuthor(Long authorId) {
        return commentRepository.countByAuthor_Id(authorId);
    }
}