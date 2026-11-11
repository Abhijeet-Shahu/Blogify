package com.bloggingapp.backend.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityCsrfTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void browserWriteWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    void browserWriteWithCsrfTokenIsAccepted() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void authenticatedBlogWriteWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/blog/save").with(user("author@example.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedBlogWriteWithCsrfTokenReachesController() throws Exception {
        mockMvc.perform(post("/blog/save")
                        .with(user("author@example.com"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
