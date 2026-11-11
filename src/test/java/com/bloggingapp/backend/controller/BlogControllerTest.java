package com.bloggingapp.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.bloggingapp.backend.model.Blog;
import com.bloggingapp.backend.model.BlogStatus;
import com.bloggingapp.backend.service.BlogService;
import com.bloggingapp.backend.service.CategoryService;
import com.bloggingapp.backend.service.UserService;

@ExtendWith(MockitoExtension.class)
class BlogControllerTest {

    @Mock
    private BlogService blogService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new BlogController(blogService, categoryService, userService)).build();
        Mockito.lenient().when(categoryService.getCategoryById(any())).thenReturn(Optional.empty());
        Mockito.lenient().when(categoryService.getAllCategories()).thenReturn(java.util.List.of());
    }

    @Test
    void allBlogsUsesPublishedStatusFilter() throws Exception {
        when(blogService.getBlogsByStatus(eq(BlogStatus.PUBLISHED), any()))
                .thenReturn(new PageImpl<>(java.util.List.of()));

        mockMvc.perform(get("/blog/all"))
                .andExpect(view().name("section"));

        verify(blogService).getBlogsByStatus(eq(BlogStatus.PUBLISHED), any());
        verify(blogService, never()).getAllBlogs(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void categoryBlogsUsePublishedStatusFilter() throws Exception {
        when(blogService.getBlogsByCategoryAndStatus(eq(7L), eq(BlogStatus.PUBLISHED), any()))
                .thenReturn(new PageImpl<>(java.util.List.of()));

        mockMvc.perform(get("/blog/category/7"))
                .andExpect(view().name("section"));

        verify(blogService).getBlogsByCategoryAndStatus(eq(7L), eq(BlogStatus.PUBLISHED), any());
    }

    @Test
    void publicDetailDoesNotIncrementViewInController() throws Exception {
        when(blogService.getBlogById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/blog/1"))
                .andExpect(view().name("redirect:/"));

        verify(blogService, never()).incrementViewCount(1L);
    }

    @Test
    void authenticatedAuthorCanAccessOwnDraft() throws Exception {
        Blog draft = blogWithStatus(BlogStatus.DRAFT);
        when(blogService.getBlogById(1L)).thenReturn(Optional.empty());
        when(blogService.getBlogByIdForAuthor(1L, "author@example.com"))
                .thenReturn(Optional.of(draft));

        mockMvc.perform(get("/blog/1")
                .principal(() -> "author@example.com"))
                .andExpect(view().name("ReadPage"));

        verify(blogService).getBlogByIdForAuthor(1L, "author@example.com");
        verify(blogService, never()).incrementViewCount(1L);
    }

    @Test
    void anotherAuthenticatedUserCannotAccessAuthorDraft() throws Exception {
        when(blogService.getBlogById(1L)).thenReturn(Optional.empty());
        when(blogService.getBlogByIdForAuthor(1L, "other@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/blog/1")
                .principal(() -> "other@example.com"))
                .andExpect(view().name("redirect:/"));

        verify(blogService).getBlogByIdForAuthor(1L, "other@example.com");
        verify(blogService, never()).incrementViewCount(1L);
    }

    @Test
    void guestCannotAccessDraft() throws Exception {
        when(blogService.getBlogById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/blog/1"))
                .andExpect(view().name("redirect:/"));

        verify(blogService, never()).getBlogByIdForAuthor(any(), any());
        verify(blogService, never()).incrementViewCount(1L);
    }

    @Test
    void authenticatedAuthorCanAccessOwnArchivedPost() throws Exception {
        Blog archived = blogWithStatus(BlogStatus.ARCHIVED);
        when(blogService.getBlogById(1L)).thenReturn(Optional.empty());
        when(blogService.getBlogByIdForAuthor(1L, "author@example.com"))
                .thenReturn(Optional.of(archived));

        mockMvc.perform(get("/blog/1")
                .principal(() -> "author@example.com"))
                .andExpect(view().name("ReadPage"));

        verify(blogService).getBlogByIdForAuthor(1L, "author@example.com");
        verify(blogService, never()).incrementViewCount(1L);
    }

    private Blog blogWithStatus(BlogStatus status) {
        Blog blog = new Blog();
        blog.setId(1L);
        blog.setStatus(status);
        return blog;
    }
}
