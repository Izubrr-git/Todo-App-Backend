package com.example.todo_application.unit.service;

import com.example.todo_application.model.User;
import com.example.todo_application.repository.UserRepository;
import com.example.todo_application.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = new User("testUser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
        testUser.setAvatarUrl("https://example.com/avatar.jpg");

        // Настройка поведения моков
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(passwordEncoder.matches(eq("correctPassword"), anyString())).thenReturn(true);
        when(passwordEncoder.matches(eq("wrongPassword"), anyString())).thenReturn(false);
    }

    @Test
    public void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserById(1L);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(1L);
        assertThat(foundUser.getUsername()).isEqualTo("testUser");
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
    }

    @Test
    public void testGetUserByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User foundUser = userService.getUserByEmail("test@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserByEmail("nonexistent@example.com"));
    }

    @Test
    public void testGetAllUsers() {
        User user2 = new User("user2", "user2@example.com", "password");
        user2.setId(2L);

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getUsername)
                .containsExactly("testUser", "user2");
    }

    @Test
    public void testCreateUser() {
        User newUser = new User("newUser", "new@example.com", "password");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User createdUser = userService.createUser(newUser);

        assertThat(createdUser).isNotNull();
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testCreateUser_EmailExists() {
        User newUser = new User("newUser", "test@example.com", "password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> userService.createUser(newUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testCreateUser_UsernameExists() {
        User newUser = new User("testUser", "new@example.com", "password");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> userService.createUser(newUser));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testUpdateUser() {
        User updatedUser = new User("updatedName", "updated@example.com", "password");
        updatedUser.setId(1L);
        updatedUser.setAvatarUrl("https://example.com/new-avatar.jpg");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(updatedUser);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("updatedName");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
        assertThat(result.getAvatarUrl()).isEqualTo("https://example.com/new-avatar.jpg");
        // Пароль не должен быть обновлен через метод updateUser
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    public void testDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    public void testDeleteUser_NotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(999L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testChangePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.changePassword(1L, "correctPassword", "newPassword");

        verify(passwordEncoder).matches("correctPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testChangePassword_WrongOldPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class,
                () -> userService.changePassword(1L, "wrongPassword", "newPassword"));

        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLoginUser_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User loggedInUser = userService.loginUser("test@example.com", "correctPassword");

        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getEmail()).isEqualTo("test@example.com");
        verify(passwordEncoder).matches("correctPassword", "encodedPassword");
    }

    @Test
    public void testLoginUser_WrongPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class,
                () -> userService.loginUser("test@example.com", "wrongPassword"));

        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
    }
}