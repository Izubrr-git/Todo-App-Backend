package com.example.todo_application.unit.validation;

import com.example.todo_application.dto.LoginRequest;
import com.example.todo_application.dto.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для проверки валидации DTO объектов.
 */
@ActiveProfiles("test")
public class ValidationTest {

    private Validator validator;

    @BeforeEach
    public void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testRegisterRequestValidation() {
        // Создание валидного запроса
        RegisterRequest validRequest = new RegisterRequest();
        validRequest.setUsername("validUser");
        validRequest.setEmail("valid@example.com");
        validRequest.setPassword("password123");

        // Проверка валидного запроса
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(validRequest);
        assertThat(violations).isEmpty();

        // Тест с пустым именем пользователя
        RegisterRequest emptyUsernameRequest = new RegisterRequest();
        emptyUsernameRequest.setUsername("");
        emptyUsernameRequest.setEmail("valid@example.com");
        emptyUsernameRequest.setPassword("password123");

        violations = validator.validate(emptyUsernameRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Username is required");

        // Тест с коротким именем пользователя
        RegisterRequest shortUsernameRequest = new RegisterRequest();
        shortUsernameRequest.setUsername("ab"); // Короче минимальной длины (3)
        shortUsernameRequest.setEmail("valid@example.com");
        shortUsernameRequest.setPassword("password123");

        violations = validator.validate(shortUsernameRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be between 3 and 20 characters long");

        // Тест с некорректным email
        RegisterRequest invalidEmailRequest = new RegisterRequest();
        invalidEmailRequest.setUsername("validUser");
        invalidEmailRequest.setEmail("not-an-email");
        invalidEmailRequest.setPassword("password123");

        violations = validator.validate(invalidEmailRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Incorrect email format");

        // Тест с коротким паролем
        RegisterRequest shortPasswordRequest = new RegisterRequest();
        shortPasswordRequest.setUsername("validUser");
        shortPasswordRequest.setEmail("valid@example.com");
        shortPasswordRequest.setPassword("12345"); // Короче минимальной длины (6)

        violations = validator.validate(shortPasswordRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password must be at least 6 characters long");
    }

    @Test
    public void testLoginRequestValidation() {
        // Создание валидного запроса
        LoginRequest validRequest = new LoginRequest();
        validRequest.setEmail("valid@example.com");
        validRequest.setPassword("password123");

        // Проверка валидного запроса
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(validRequest);
        assertThat(violations).isEmpty();

        // Тест с пустым email
        LoginRequest emptyEmailRequest = new LoginRequest();
        emptyEmailRequest.setEmail("");
        emptyEmailRequest.setPassword("password123");

        violations = validator.validate(emptyEmailRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Email is required");

        // Тест с пустым паролем
        LoginRequest emptyPasswordRequest = new LoginRequest();
        emptyPasswordRequest.setEmail("valid@example.com");
        emptyPasswordRequest.setPassword("");

        violations = validator.validate(emptyPasswordRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Password is required");
    }
}