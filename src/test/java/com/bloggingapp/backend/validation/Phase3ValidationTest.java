package com.bloggingapp.backend.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.bloggingapp.backend.dto.AuthRequest;
import com.bloggingapp.backend.dto.BlogRequest;
import com.bloggingapp.backend.dto.PasswordChangeRequest;
import com.bloggingapp.backend.dto.RegisterRequest;

class Phase3ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void registrationRequiresConfirmationAndValidCredentials() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("writer");
        request.setEmail("writer@example.com");
        request.setPassword("secret");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void loginRequiresEmailAndPassword() {
        AuthRequest request = new AuthRequest();

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void blogRequestRequiresContentAndCategory() {
        BlogRequest request = new BlogRequest();
        request.setTitle("A title");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void passwordChangeRequiresStrongMatchingFields() {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("short");
        request.setConfirmPassword("different");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void validLoginRequestPassesValidation() {
        AuthRequest request = new AuthRequest();
        request.setEmail("writer@example.com");
        request.setPassword("secret");

        assertTrue(validator.validate(request).isEmpty());
    }
}
