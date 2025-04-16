package com.example.todo_application.integration.service;

import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import com.example.todo_application.repository.TodoRepository;
import com.example.todo_application.repository.UserRepository;
import com.example.todo_application.integration.BaseIntegrationTest;
import com.example.todo_application.service.TodoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Интеграционный тест для TodoService, использующий реальную базу данных PostgreSQL
 * через Testcontainers.
 */
@SpringBootTest
@ActiveProfiles("test")
public class TodoServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    public void setup() {
        // Очистка базы данных перед каждым тестом
        todoRepository.deleteAll();
        userRepository.deleteAll();

        // Создание тестовых пользователей
        testUser = new User("testUser", "test@example.com", "password123");
        testUser = userRepository.save(testUser);

        anotherUser = new User("anotherUser", "another@example.com", "password456");
        anotherUser = userRepository.save(anotherUser);
    }

    @AfterEach
    public void cleanup() {
        todoRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testCreateAndGetTodosByUserId() {
        // Создание задачи для пользователя
        Todo todo = new Todo("Test Task", false);
        Todo createdTodo = todoService.createTodo(testUser.getId(), todo);

        // Проверка, что ID присвоен
        assertThat(createdTodo.getId()).isNotNull();

        // Добавление еще одной задачи
        Todo secondTodo = new Todo("Second Task", true);
        todoService.createTodo(testUser.getId(), secondTodo);

        // Получение всех задач пользователя
        List<Todo> userTodos = todoService.getAllTodosByUserId(testUser.getId());

        // Проверка, что обе задачи получены
        assertThat(userTodos).hasSize(2);
        assertThat(userTodos).extracting(Todo::getTaskText)
                .containsExactlyInAnyOrder("Test Task", "Second Task");
    }

    @Test
    public void testCreateTodo_UserNotFound() {
        // Попытка создать задачу для несуществующего пользователя
        Todo todo = new Todo("Test Task", false);

        assertThrows(RuntimeException.class, () -> todoService.createTodo(999L, todo));
    }

    @Test
    public void testUpdateTodo() {
        // Создание задачи
        Todo todo = new Todo("Original Task", false);
        Todo createdTodo = todoService.createTodo(testUser.getId(), todo);

        // Обновление задачи
        Todo todoDetails = new Todo("Updated Task", true);
        Todo updatedTodo = todoService.updateTodo(testUser.getId(), createdTodo.getId(), todoDetails);

        // Проверка обновленных данных
        assertThat(updatedTodo.getTaskText()).isEqualTo("Updated Task");
        assertThat(updatedTodo.isDone()).isTrue();

        // Проверка, что пользователь не изменился
        assertThat(updatedTodo.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    public void testUpdateTodo_TodoNotFound() {
        // Попытка обновить несуществующую задачу
        Todo todoDetails = new Todo("Updated Task", true);

        assertThrows(RuntimeException.class,
                () -> todoService.updateTodo(testUser.getId(), 999L, todoDetails));
    }

    @Test
    public void testUpdateTodo_TodoNotBelongToUser() {
        // Создание задачи для одного пользователя
        Todo todo = new Todo("Original Task", false);
        Todo createdTodo = todoService.createTodo(testUser.getId(), todo);

        // Попытка обновить задачу от имени другого пользователя
        Todo todoDetails = new Todo("Updated Task", true);

        assertThrows(RuntimeException.class,
                () -> todoService.updateTodo(anotherUser.getId(), createdTodo.getId(), todoDetails));
    }

    @Test
    public void testDeleteTodo() {
        // Создание задачи
        Todo todo = new Todo("Task to Delete", false);
        Todo createdTodo = todoService.createTodo(testUser.getId(), todo);

        // Удаление задачи
        todoService.deleteTodo(testUser.getId(), createdTodo.getId());

        // Проверка, что задача удалена
        List<Todo> userTodos = todoService.getAllTodosByUserId(testUser.getId());
        assertThat(userTodos).isEmpty();
    }

    @Test
    public void testDeleteTodo_TodoNotFound() {
        // Попытка удалить несуществующую задачу
        assertThrows(RuntimeException.class,
                () -> todoService.deleteTodo(testUser.getId(), 999L));
    }

    @Test
    public void testDeleteTodo_TodoNotBelongToUser() {
        // Создание задачи для одного пользователя
        Todo todo = new Todo("Task to Delete", false);
        Todo createdTodo = todoService.createTodo(testUser.getId(), todo);

        // Попытка удалить задачу от имени другого пользователя
        assertThrows(RuntimeException.class,
                () -> todoService.deleteTodo(anotherUser.getId(), createdTodo.getId()));
    }

    @Test
    public void testGetAllTodosByUserId_MultipleUsers() {
        // Создание задач для первого пользователя
        todoService.createTodo(testUser.getId(), new Todo("User 1 Task 1", false));
        todoService.createTodo(testUser.getId(), new Todo("User 1 Task 2", true));

        // Создание задач для второго пользователя
        todoService.createTodo(anotherUser.getId(), new Todo("User 2 Task 1", false));

        // Проверка задач первого пользователя
        List<Todo> user1Todos = todoService.getAllTodosByUserId(testUser.getId());
        assertThat(user1Todos).hasSize(2);
        assertThat(user1Todos).extracting(Todo::getTaskText)
                .containsExactlyInAnyOrder("User 1 Task 1", "User 1 Task 2");

        // Проверка задач второго пользователя
        List<Todo> user2Todos = todoService.getAllTodosByUserId(anotherUser.getId());
        assertThat(user2Todos).hasSize(1);
        assertThat(user2Todos).extracting(Todo::getTaskText)
                .containsExactly("User 2 Task 1");
    }
}