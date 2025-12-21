package com.bloggingapp.backend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bloggingapp.backend.dto.AuthRequest;
import com.bloggingapp.backend.dto.RegisterRequest;
import com.bloggingapp.backend.dto.UserProfileDTO;
import com.bloggingapp.backend.model.Blog;
import com.bloggingapp.backend.model.BlogStatus;
import com.bloggingapp.backend.model.User;
import com.bloggingapp.backend.repository.BlogRepository;
import com.bloggingapp.backend.repository.UserRepository;
import com.bloggingapp.backend.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final String PROFILE_PICTURE_DIR = "src/main/resources/static/uploads/profiles/";

    public UserServiceImpl(UserRepository userRepository, BlogRepository blogRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.blogRepository = blogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // NEW METHOD: DTO-based profile update
    @Override
    public User updateProfile(UserProfileDTO userProfileDTO, String email, MultipartFile profilePicture) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user fields from DTO
        existingUser.setFullName(userProfileDTO.getFullName());
        existingUser.setTagline(userProfileDTO.getTagline());
        existingUser.setBio(userProfileDTO.getBio());
        existingUser.setPhoneNumber(userProfileDTO.getPhoneNumber());
        existingUser.setAddress(userProfileDTO.getAddress());
        existingUser.setTwitterHandle(userProfileDTO.getTwitterHandle());
        existingUser.setLinkedinUrl(userProfileDTO.getLinkedinUrl());
        existingUser.setWebsite(userProfileDTO.getWebsite());

        // Convert String date to LocalDate
        if (userProfileDTO.getDateOfBirth() != null && !userProfileDTO.getDateOfBirth().isEmpty()) {
            try {
                LocalDate dob = LocalDate.parse(userProfileDTO.getDateOfBirth());
                existingUser.setDateOfBirth(dob);
            } catch (Exception e) {
                throw new RuntimeException("Invalid date format. Please use YYYY-MM-DD format.");
            }
        }

        // Handle profile picture upload
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String imageUrl = saveProfilePicture(profilePicture);
            existingUser.setProfilePicture(imageUrl);
        }

        return userRepository.save(existingUser);
    }

    // OLD METHOD: Keep for backward compatibility or remove it
    @Override
    public User updateProfile(User user, String email, MultipartFile profilePicture) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setFullName(user.getFullName());
        existingUser.setTagline(user.getTagline());
        existingUser.setBio(user.getBio());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setAddress(user.getAddress());
        existingUser.setTwitterHandle(user.getTwitterHandle());
        existingUser.setLinkedinUrl(user.getLinkedinUrl());
        existingUser.setWebsite(user.getWebsite());
        existingUser.setDateOfBirth(user.getDateOfBirth());

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String imageUrl = saveProfilePicture(profilePicture);
            existingUser.setProfilePicture(imageUrl);
        }

        return userRepository.save(existingUser);
    }

    @Override
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists with email: " + request.getEmail());
        }
        
        if (existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEnabled(true);
        
        return userRepository.save(user);
    }

    // Rest of your existing methods remain the same...
    @Override
    public User loginUser(AuthRequest authRequest) {
        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + authRequest.getEmail()));
        
        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        if (!user.isEnabled()) {
            throw new RuntimeException("Account is disabled");
        }
        
        userRepository.updateLastLogin(user.getEmail());
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findActiveUsers(pageable);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        userRepository.delete(user);
    }

    @Override
    public User updatePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        Long postsPublished = blogRepository.countByAuthor_IdAndStatus(userId, BlogStatus.PUBLISHED);
        Long totalViews = blogRepository.findByAuthor_Id(userId).stream()
                .mapToLong(Blog::getViewCount)
                .sum();
        Long followers = 0L;
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        stats.put("postsPublished", postsPublished);
        stats.put("totalViews", totalViews);
        stats.put("followers", followers);
        stats.put("joinedDate", user.getCreatedAt());

        return stats;
    }

    @Override
    public List<Map<String, Object>> getUserRecentPosts(Long userId) {
        List<Blog> recentBlogs = blogRepository.findByAuthor_Id(userId).stream()
                .limit(6)
                .collect(Collectors.toList());
        
        return recentBlogs.stream().map(blog -> {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", blog.getId());
            postMap.put("title", blog.getTitle());
            postMap.put("excerpt", blog.getExcerpt());
            postMap.put("featuredImage", blog.getFeaturedImage());
            postMap.put("category", blog.getCategory());
            postMap.put("createdAt", blog.getCreatedAt());
            return postMap;
        }).collect(Collectors.toList());
    }

    @Override
    public Long getWriterCount() {
        return userRepository.countWriters();
    }

    @Override
    public Long getReaderCount() {
        return userRepository.count() - userRepository.countWriters();
    }

    @Override
    public Page<User> getPopularWriters(Pageable pageable) {
        return userRepository.findPopularWriters(pageable)
                .map(result -> (User) ((Object[]) result)[0]);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    private String saveProfilePicture(MultipartFile image) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
            Path filePath = Paths.get(PROFILE_PICTURE_DIR + fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, image.getBytes());
            return "/uploads/profiles/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile picture: " + e.getMessage());
        }
    }
}