package com.bloggingapp.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.bloggingapp.backend.model.Blog;
import com.bloggingapp.backend.model.BlogStatus;
import com.bloggingapp.backend.repository.BlogRepository;
import com.bloggingapp.backend.repository.CategoryRepository;
import com.bloggingapp.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BlogServiceImplTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private BlogServiceImpl blogService;

    @Test
    void publicBlogLookupReturnsPublishedBlogAndIncrementsViewOnce() {
        Blog blog = blogWithStatus(BlogStatus.PUBLISHED);
        when(blogRepository.findByIdAndStatus(1L, BlogStatus.PUBLISHED))
                .thenReturn(Optional.of(blog));
        ReflectionTestUtils.setField(blogService, "entityManager", entityManager);

        assertTrue(blogService.getBlogById(1L).isPresent());

        verify(blogRepository).incrementViewCount(1L);
        verify(entityManager).refresh(blog);
    }

    @Test
    void publicBlogLookupDoesNotReturnDraftOrIncrementView() {
        when(blogRepository.findByIdAndStatus(1L, BlogStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertTrue(blogService.getBlogById(1L).isEmpty());

        verify(blogRepository, never()).incrementViewCount(1L);
    }

    @Test
    void publicBlogLookupDoesNotReturnArchivedBlog() {
        when(blogRepository.findByIdAndStatus(1L, BlogStatus.PUBLISHED))
                .thenReturn(Optional.empty());

        assertTrue(blogService.getBlogById(1L).isEmpty());

        verify(blogRepository).findByIdAndStatus(1L, BlogStatus.PUBLISHED);
    }

    @Test
    void authorLookupReturnsOnlyBlogOwnedByAuthenticatedAuthor() {
        Blog blog = blogWithStatus(BlogStatus.DRAFT);
        when(blogRepository.findByIdAndAuthor_Email(1L, "author@example.com"))
                .thenReturn(Optional.of(blog));

        assertTrue(blogService.getBlogByIdForAuthor(1L, "author@example.com").isPresent());
        verify(blogRepository).findByIdAndAuthor_Email(1L, "author@example.com");
        verify(blogRepository, never()).incrementViewCount(1L);
    }

    @Test
    void otherAuthorCannotLoadUnpublishedBlog() {
        when(blogRepository.findByIdAndAuthor_Email(1L, "other@example.com"))
                .thenReturn(Optional.empty());

        assertTrue(blogService.getBlogByIdForAuthor(1L, "other@example.com").isEmpty());
    }

    private Blog blogWithStatus(BlogStatus status) {
        Blog blog = new Blog();
        blog.setId(1L);
        blog.setStatus(status);
        return blog;
    }
}
