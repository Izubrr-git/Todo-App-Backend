package com.example.todo_application.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testPasswordEncoder() {
        // Encoding check
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Checking that the password has been encrypted
        assertNotEquals(rawPassword, encodedPassword);

        // Checking that the password can be verified
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));

        // Checking that an incorrect password will not pass verification
        assertFalse(passwordEncoder.matches("wrongPassword", encodedPassword));
    }

    @Test
    void testPasswordEncoderGeneratesDifferentHashesForSamePassword() {
        // Checking that a new salt (and therefore a new hash) is generated each time
        String password = "testPassword";
        String firstHash = passwordEncoder.encode(password);
        String secondHash = passwordEncoder.encode(password);

        // The hashes should be different because of the different salts
        assertNotEquals(firstHash, secondHash);

        // Both must pass verification
        assertTrue(passwordEncoder.matches(password, firstHash));
        assertTrue(passwordEncoder.matches(password, secondHash));
    }
}