package com.bloggingapp.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.bloggingapp.backend.dto.AuthRequest;
import com.bloggingapp.backend.dto.RegisterRequest;
import com.bloggingapp.backend.dto.UserProfileDTO;
import com.bloggingapp.backend.model.User;

public interface UserService {

    User registerUser(RegisterRequest request);
    User loginUser(AuthRequest authRequest);
    Optional<User> findByEmail(String email);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUser();
    Page<User> getAllUsers(Pageable pageable);
    Optional<User> getUserById(Long id);
    void deleteUser(Long id);
    void deleteUserByEmail(String email);
    User updateProfile(UserProfileDTO userProfileDTO, String email, MultipartFile profilePicture);   
    User updatePassword(String email, String currentPassword, String newPassword);
    Map<String, Object> getUserStats(Long userId);
    List<Map<String, Object>> getUserRecentPosts(Long userId);
    Long getWriterCount();
    Long getReaderCount();
    Page<User> getPopularWriters(Pageable pageable);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User updateProfile(User user, String email, MultipartFile profilePicture);
}