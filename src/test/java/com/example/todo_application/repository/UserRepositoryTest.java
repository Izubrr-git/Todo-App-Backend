package com.example.todo_application.repository;

import com.example.todo_application.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE) // Используем реальную базу данных вместо встроенной
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCreateAndFindUser() {
        // Создаем тестового пользователя
        User user = new User("testUser", "test@example.com", "password123");
        user.setAvatarUrl("https://example.com/avatar.jpg");

        // Сохраняем пользователя
        User savedUser = userRepository.save(user);

        // Проверяем, что ID присвоен
        assertThat(savedUser.getId()).isNotNull();

        // Находим пользователя по ID
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testUser");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void testFindByEmail() {
        // Создаем тестового пользователя
        User user = new User("emailUser", "email@example.com", "password123");
        userRepository.save(user);

        // Находим пользователя по email
        Optional<User> foundUser = userRepository.findByEmail("email@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("emailUser");
    }

    @Test
    public void testFindByUsername() {
        // Создаем тестового пользователя
        User user = new User("usernameTest", "username@example.com", "password123");
        userRepository.save(user);

        // Находим пользователя по username
        Optional<User> foundUser = userRepository.findByUsername("usernameTest");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("username@example.com");
    }

    @Test
    public void testDeleteUser() {
        // Создаем тестового пользователя
        User user = new User("deleteUser", "delete@example.com", "password123");
        User savedUser = userRepository.save(user);

        // Удаляем пользователя
        userRepository.deleteById(savedUser.getId());

        // Проверяем, что пользователь удален
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }
}