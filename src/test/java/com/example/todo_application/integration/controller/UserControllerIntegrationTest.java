package com.example.todo_application.integration.controller;

import com.example.todo_application.dto.LoginRequest;
import com.example.todo_application.dto.RegisterRequest;
import com.example.todo_application.dto.UserDTO;
import com.example.todo_application.model.User;
import com.example.todo_application.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    public void setup() {
        // Создаем тестового пользователя
        testUser = new User("testUser", "test@example.com", passwordEncoder.encode("password123"));
        testUser.setAvatarUrl("https://example.com/avatar.jpg");
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    public void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    public void testGetUserById() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.jpg"));
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User not found with id 999"));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        // Создаем второго пользователя
        User user2 = new User("user2", "user2@example.com", passwordEncoder.encode("password"));
        userRepository.save(user2);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void testRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setAvatarUrl("https://example.com/new-avatar.jpg");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/new-avatar.jpg"));

        // Проверка, что пользователь создан в базе данных
        assertThat(userRepository.findByEmail("new@example.com")).isPresent();
    }

    @Test
    public void testRegisterUser_EmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("anotherUser");
        request.setEmail("test@example.com"); // Email уже существует
        request.setPassword("password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    public void testLoginUser() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void testLoginUser_InvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid password"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("updatedName");
        userDTO.setEmail("updated@example.com");
        userDTO.setAvatarUrl("https://example.com/new-avatar.jpg");

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedName"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/new-avatar.jpg"));

        // Проверка, что пользователь обновлен в базе данных
        User updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo("updatedName");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk());

        // Проверка, что пользователь удален из базы данных
        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }

    @Test
    public void testChangePassword() throws Exception {
        mockMvc.perform(post("/api/users/{id}/change-password", testUser.getId())
                        .param("oldPassword", "password123")
                        .param("newPassword", "newPassword123"))
                .andExpect(status().isOk());

        // Проверка, что пароль изменен
        User updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(passwordEncoder.matches("newPassword123", updatedUser.getPassword())).isTrue();
    }

    @Test
    public void testChangePassword_WrongOldPassword() throws Exception {
        mockMvc.perform(post("/api/users/{id}/change-password", testUser.getId())
                        .param("oldPassword", "wrongPassword")
                        .param("newPassword", "newPassword123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Old password is incorrect"));
    }
}