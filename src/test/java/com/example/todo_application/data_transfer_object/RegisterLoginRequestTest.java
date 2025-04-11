package com.example.todo_application.data_transfer_object;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegisterLoginRequestTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRegisterRequest() {
        // Valid register request
        RegisterRequest request = new RegisterRequest();
        request.setUsername("validuser");
        request.setEmail("valid@example.com");
        request.setPassword("validpassword");
        request.setAvatarUrl("avatar.jpg");

        // Validate the request
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Check for validation errors
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidRegisterRequest_EmptyUsername() {
        // Empty username request
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setEmail("valid@example.com");
        request.setPassword("validpassword");

        // Validate the request
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Check for validation errors
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        // Check the error message
        ConstraintViolation<RegisterRequest> violation = violations.iterator().next();
        assertEquals("Username is required", violation.getMessage());
    }

    @Test
    void testInvalidRegisterRequest_ShortUsername() {
        // Too short username request
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab"); // Less than 3 characters
        request.setEmail("valid@example.com");
        request.setPassword("validpassword");

        // Validate the request
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Check for validation errors
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        // Check the error message
        ConstraintViolation<RegisterRequest> violation = violations.iterator().next();
        assertEquals("Username must be between 3 and 20 characters long", violation.getMessage());
    }

    @Test
    void testInvalidRegisterRequest_InvalidEmail() {
        // Incorrect email request
        RegisterRequest request = new RegisterRequest();
        request.setUsername("validuser");
        request.setEmail("invalid-email");
        request.setPassword("validpassword");

        // Validate the request
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Check for validation errors
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        // Check the error message
        ConstraintViolation<RegisterRequest> violation = violations.iterator().next();
        assertEquals("Incorrect email format", violation.getMessage());
    }

    @Test
    void testInvalidRegisterRequest_ShortPassword() {
        // Too short password request
        RegisterRequest request = new RegisterRequest();
        request.setUsername("validuser");
        request.setEmail("valid@example.com");
        request.setPassword("12345"); // Less than 6 characters

        // Validate the request
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Check for validation errors
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        // Check the error message
        ConstraintViolation<RegisterRequest> violation = violations.iterator().next();
        assertEquals("Password must be at least 6 characters long", violation.getMessage());
    }

    @Test
    void testValidLoginRequest() {
        // Valid login request
        LoginRequest request = new LoginRequest();
        request.setEmail("valid@example.com");
        request.setPassword("validpassword");

        // Validate the request
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Check for validation errors
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidLoginRequest_EmptyEmail() {
        // Empty email request
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("validpassword");

        // Validate the request
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Check for validation errors
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        // Check the error message
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("Email is required", violation.getMessage());
    }

    @Test
    void testInvalidLoginRequest_EmptyPassword() {
        // Empty password request
        LoginRequest request = new LoginRequest();
        request.setEmail("valid@example.com");
        request.setPassword("");

        // Validate the request
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Check for validation errors
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        // Check the error message
        ConstraintViolation<LoginRequest> violation = violations.iterator().next();
        assertEquals("Password is required", violation.getMessage());
    }
}
