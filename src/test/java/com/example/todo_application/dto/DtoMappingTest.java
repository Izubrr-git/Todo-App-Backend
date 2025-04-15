package com.example.todo_application.dto;

import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для проверки маппинга между DTO и сущностями.
 */
@ActiveProfiles("test")
public class DtoMappingTest {

    @Test
    public void testUserToUserDtoMapping() {
        // Создание пользователя с задачами
        User user = new User("testUser", "test@example.com", "password123");
        user.setId(1L);
        user.setAvatarUrl("https://example.com/avatar.jpg");

        Todo todo1 = new Todo("Task 1", false);
        todo1.setId(1L);
        todo1.setUser(user);

        Todo todo2 = new Todo("Task 2", true);
        todo2.setId(2L);
        todo2.setUser(user);

        user.setTodos(new ArrayList<>());
        user.getTodos().add(todo1);
        user.getTodos().add(todo2);

        // Преобразование в DTO
        UserDTO userDTO = new UserDTO(user);

        // Проверка маппинга полей
        assertThat(userDTO.getId()).isEqualTo(1L);
        assertThat(userDTO.getUsername()).isEqualTo("testUser");
        assertThat(userDTO.getEmail()).isEqualTo("test@example.com");
        assertThat(userDTO.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");

        // Проверка маппинга задач
        assertThat(userDTO.getTodos()).hasSize(2);
        assertThat(userDTO.getTodos().get(0).getId()).isEqualTo(1L);
        assertThat(userDTO.getTodos().get(0).getTaskText()).isEqualTo("Task 1");
        assertThat(userDTO.getTodos().get(0).isDone()).isFalse();
        assertThat(userDTO.getTodos().get(1).getId()).isEqualTo(2L);
        assertThat(userDTO.getTodos().get(1).getTaskText()).isEqualTo("Task 2");
        assertThat(userDTO.getTodos().get(1).isDone()).isTrue();
    }

    @Test
    public void testUserDtoToUserMapping() {
        // Создание DTO пользователя
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testUser");
        userDTO.setEmail("test@example.com");
        userDTO.setAvatarUrl("https://example.com/avatar.jpg");

        // Преобразование в сущность
        User user = userDTO.toEntity();

        // Проверка маппинга полей
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testUser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getAvatarUrl()).isEqualTo("https://example.com/avatar.jpg");

        // Пароль не должен передаваться через DTO
        assertThat(user.getPassword()).isNull();

        // Задачи не должны маппиться из DTO в сущность
        assertThat(user.getTodos()).isEmpty();
    }

    @Test
    public void testTodoToTodoDtoMapping() {
        // Создание пользователя
        User user = new User("testUser", "test@example.com", "password123");
        user.setId(1L);

        // Создание задачи
        Todo todo = new Todo("Test Task", true);
        todo.setId(1L);
        todo.setUser(user);

        // Преобразование в DTO
        TodoDTO todoDTO = new TodoDTO(todo);

        // Проверка маппинга полей
        assertThat(todoDTO.getId()).isEqualTo(1L);
        assertThat(todoDTO.getTaskText()).isEqualTo("Test Task");
        assertThat(todoDTO.isDone()).isTrue();

        // Проверка маппинга информации о пользователе
        assertThat(todoDTO.getUserId()).isEqualTo(1L);
        assertThat(todoDTO.getUsername()).isEqualTo("testUser");
    }

    @Test
    public void testTodoDtoToTodoMapping() {
        // Создание DTO задачи
        TodoDTO todoDTO = new TodoDTO();
        todoDTO.setId(1L);
        todoDTO.setTaskText("Test Task");
        todoDTO.setDone(true);
        todoDTO.setUserId(1L);
        todoDTO.setUsername("testUser");

        // Преобразование в сущность
        Todo todo = todoDTO.toEntity();

        // Проверка маппинга полей
        assertThat(todo.getId()).isEqualTo(1L);
        assertThat(todo.getTaskText()).isEqualTo("Test Task");
        assertThat(todo.isDone()).isTrue();

        // Пользователь не должен маппиться из DTO в сущность
        assertThat(todo.getUser()).isNull();
    }

    @Test
    public void testUserDtoWithNullTodos() {
        // Создание пользователя без задач
        User user = new User("testUser", "test@example.com", "password123");
        user.setId(1L);
        user.setTodos(null); // Установка задач в null

        // Преобразование в DTO
        UserDTO userDTO = new UserDTO(user);

        // Проверка, что список задач инициализирован как пустой список
        assertThat(userDTO.getTodos()).isNotNull();
        assertThat(userDTO.getTodos()).isEmpty();
    }
}