package com.bloggingapp.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;   
    private String password;
    private boolean enabled;
    private String role = "USER";

    // Changed back to fullName
    private String fullName;
    private String tagline;
    private String bio;
    private String profilePicture;
    private String phoneNumber;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    private String address;
    private String twitterHandle;
    private String linkedinUrl;
    private String website;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @ToString.Exclude
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Blog> blogs;
}