package com.example.todo_application.service;

import com.example.todo_application.model.User;
import com.example.todo_application.repository.UserRepository;
import com.example.todo_application.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Интеграционный тест для UserService, использующий реальную базу данных PostgreSQL
 * через Testcontainers.
 */
@SpringBootTest
@ActiveProfiles("test")
public class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    public void setup() {
        // Очистка базы данных перед каждым тестом
        userRepository.deleteAll();

        // Создание тестового пользователя
        testUser = new User("testUser", "test@example.com", "password123");
    }

    @AfterEach
    public void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    public void testCreateAndGetUser() {
        // Создание пользователя через сервис
        User createdUser = userService.createUser(testUser);

        // Проверка, что ID присвоен
        assertThat(createdUser.getId()).isNotNull();

        // Получение пользователя по ID
        User foundUser = userService.getUserById(createdUser.getId());

        // Проверка данных пользователя
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testUser");
        assertThat(foundUser.getEmail()).isEqualTo("test@example.com");

        // Проверка, что пароль зашифрован
        assertThat(foundUser.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", foundUser.getPassword())).isTrue();
    }

    @Test
    public void testGetAllUsers() {
        // Создание нескольких пользователей
        userService.createUser(testUser);

        User secondUser = new User("secondUser", "second@example.com", "password456");
        userService.createUser(secondUser);

        // Получение всех пользователей
        List<User> allUsers = userService.getAllUsers();

        // Проверка, что оба пользователя возвращены
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getUsername)
                .containsExactlyInAnyOrder("testUser", "secondUser");
    }

    @Test
    public void testUpdateUser() {
        // Создание пользователя
        User createdUser = userService.createUser(testUser);

        // Обновление пользователя
        createdUser.setUsername("updatedUsername");
        createdUser.setEmail("updated@example.com");
        createdUser.setAvatarUrl("https://example.com/avatar.jpg");

        User updatedUser = userService.updateUser(createdUser);

        // Проверка обновленных данных
        assertThat(updatedUser.getUsername()).isEqualTo("updatedUsername");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");

        // Пароль не должен измениться при обновлении
        assertThat(passwordEncoder.matches("password123", updatedUser.getPassword())).isTrue();
    }

    @Test
    public void testDeleteUser() {
        // Создание пользователя
        User createdUser = userService.createUser(testUser);

        // Удаление пользователя
        userService.deleteUser(createdUser.getId());

        // Проверка, что пользователь удален
        assertThrows(RuntimeException.class, () -> userService.getUserById(createdUser.getId()));
    }

    @Test
    public void testLoginUser() {
        // Создание пользователя
        userService.createUser(testUser);

        // Успешный вход
        User loggedInUser = userService.loginUser("test@example.com", "password123");
        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getUsername()).isEqualTo("testUser");

        // Неверный пароль
        assertThrows(RuntimeException.class,
                () -> userService.loginUser("test@example.com", "wrongPassword"));
    }

    @Test
    public void testChangePassword() {
        // Создание пользователя
        User createdUser = userService.createUser(testUser);

        // Изменение пароля
        userService.changePassword(createdUser.getId(), "password123", "newPassword");

        // Проверка, что пароль изменен
        User updatedUser = userService.getUserById(createdUser.getId());
        assertThat(passwordEncoder.matches("newPassword", updatedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("password123", updatedUser.getPassword())).isFalse();
    }

    @Test
    public void testDuplicateEmail() {
        // Создание первого пользователя
        userService.createUser(testUser);

        // Попытка создать пользователя с тем же email
        User duplicateUser = new User("anotherUser", "test@example.com", "password456");

        // Должно выбросить исключение
        assertThrows(RuntimeException.class, () -> userService.createUser(duplicateUser));
    }

    @Test
    public void testDuplicateUsername() {
        // Создание первого пользователя
        userService.createUser(testUser);

        // Попытка создать пользователя с тем же username
        User duplicateUser = new User("testUser", "another@example.com", "password456");

        // Должно выбросить исключение
        assertThrows(RuntimeException.class, () -> userService.createUser(duplicateUser));
    }
}