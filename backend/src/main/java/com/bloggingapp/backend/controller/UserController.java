package com.bloggingapp.backend.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bloggingapp.backend.dto.UserProfileDTO;
import com.bloggingapp.backend.model.User;
import com.bloggingapp.backend.service.UserService;

@Controller
@RequestMapping("/profile")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Show user profile
    @GetMapping
    public String showProfile(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        Optional<User> user = userService.getUserByEmail(principal.getName());
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        model.addAttribute("user", user.get());
        model.addAttribute("userStats", userService.getUserStats(user.get().getId()));
        model.addAttribute("recentPosts", userService.getUserRecentPosts(user.get().getId()));

        return "Profile";
    }

    // Show edit profile page - UPDATED
    @GetMapping("/edit")
    public String showEditProfile(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        Optional<User> user = userService.getUserByEmail(principal.getName());
        if (user.isEmpty()) {
            return "redirect:/login";
        }

        // Convert User to UserProfileDTO for the form
        User userEntity = user.get();
        UserProfileDTO userProfileDTO = new UserProfileDTO();
        
        // Set basic info
        userProfileDTO.setFullName(userEntity.getFullName());
        userProfileDTO.setTagline(userEntity.getTagline());
        userProfileDTO.setBio(userEntity.getBio());
        userProfileDTO.setPhoneNumber(userEntity.getPhoneNumber());
        
        // Set date of birth if exists
        if (userEntity.getDateOfBirth() != null) {
            userProfileDTO.setDateOfBirth(userEntity.getDateOfBirth().toString());
        }
        
        // Set social and contact info
        userProfileDTO.setAddress(userEntity.getAddress());
        userProfileDTO.setTwitterHandle(userEntity.getTwitterHandle());
        userProfileDTO.setLinkedinUrl(userEntity.getLinkedinUrl());
        userProfileDTO.setWebsite(userEntity.getWebsite());

        model.addAttribute("userProfileDTO", userProfileDTO);
        model.addAttribute("user", userEntity);
        model.addAttribute("userStats", userService.getUserStats(userEntity.getId()));

        return "CompleteProfile";
    }

    // Update profile - UPDATED to use DTO
    @PostMapping("/complete")
    public String updateProfile(@ModelAttribute UserProfileDTO userProfileDTO,
                               @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User updatedUser = userService.updateProfile(userProfileDTO, principal.getName(), profilePicture);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    // Change password page
    @GetMapping("/change-password")
    public String showChangePasswordPage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        return "change-password";
    }

    // Delete account
    @PostMapping("/delete")
    public String deleteAccount(Principal principal, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUserByEmail(principal.getName());
            redirectAttributes.addFlashAttribute("success", "Account deleted successfully!");
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile";
        }
    }
}