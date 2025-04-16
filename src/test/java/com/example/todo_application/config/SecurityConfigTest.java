package com.example.todo_application.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(SecurityConfig.class)
@ActiveProfiles("test")
public class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testPasswordEncoder() {
        // Проверка того, что PasswordEncoder успешно внедрен
        assertThat(passwordEncoder).isNotNull();

        // Проверка шифрования пароля
        String password = "testPassword";
        String encodedPassword = passwordEncoder.encode(password);

        // Проверка, что пароль зашифрован (не равен исходному)
        assertThat(encodedPassword).isNotEqualTo(password);

        // Проверка, что метод matches работает правильно
        assertThat(passwordEncoder.matches(password, encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
    }
}