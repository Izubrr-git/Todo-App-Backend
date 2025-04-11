package com.example.todo_application.service;

import com.example.todo_application.model.User;
import com.example.todo_application.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User found = userService.getUserById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("testuser", found.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
    }

    @Test
    void testGetUserByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User found = userService.getUserByEmail("test@example.com");

        assertNotNull(found);
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserByEmail("notfound@example.com"));
    }

    @Test
    void testGetAllUsers() {
        User user2 = new User("user2", "user2@example.com", "password");
        user2.setId(2L);

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<User> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    void testCreateUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User newUser = new User("newuser", "new@example.com", "password");
        User created = userService.createUser(newUser);

        assertNotNull(created);
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_EmailExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User newUser = new User("newuser", "test@example.com", "password");

        assertThrows(RuntimeException.class, () -> userService.createUser(newUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_UsernameExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User newUser = new User("testuser", "new@example.com", "password");

        assertThrows(RuntimeException.class, () -> userService.createUser(newUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUser() {
        User existingUser = new User("testuser", "test@example.com", "encodedPassword");
        existingUser.setId(1L);
        existingUser.setAvatarUrl("old-avatar.jpg");

        User updatedData = new User("updated", "updated@example.com", "password");
        updatedData.setId(1L);
        updatedData.setAvatarUrl("new-avatar.jpg");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(updatedData);

        assertNotNull(result);
        assertEquals("updated", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("new-avatar.jpg", result.getAvatarUrl());
        // Пароль не должен обновляться при обычном обновлении пользователя
        assertEquals("encodedPassword", result.getPassword());
    }

    @Test
    void testDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(999L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testChangePassword() {
        User user = new User("testuser", "test@example.com", "encodedOldPassword");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changePassword(1L, "oldPassword", "newPassword");

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        User user = new User("testuser", "test@example.com", "encodedOldPassword");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                userService.changePassword(1L, "wrongPassword", "newPassword"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginUser() {
        User user = new User("testuser", "test@example.com", "encodedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        User loggedIn = userService.loginUser("test@example.com", "password");

        assertNotNull(loggedIn);
        assertEquals("test@example.com", loggedIn.getEmail());
    }

    @Test
    void testLoginUser_InvalidPassword() {
        User user = new User("testuser", "test@example.com", "encodedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                userService.loginUser("test@example.com", "wrongPassword"));
    }
}