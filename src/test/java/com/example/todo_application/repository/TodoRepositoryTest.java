package com.example.todo_application.repository;

import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE) // Используем реальную базу данных вместо встроенной
public class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setup() {
        // Создаем тестового пользователя перед каждым тестом
        testUser = new User("todoUser", "todo@example.com", "password123");
        testUser = userRepository.save(testUser);
    }

    @Test
    public void testCreateAndFindTodo() {
        // Создаем задачу
        Todo todo = new Todo("Test Task", false);
        todo.setUser(testUser);

        // Сохраняем задачу
        Todo savedTodo = todoRepository.save(todo);

        // Проверяем, что ID присвоен
        assertThat(savedTodo.getId()).isNotNull();

        // Находим задачу по ID
        Todo foundTodo = todoRepository.findById(savedTodo.getId()).orElse(null);
        assertThat(foundTodo).isNotNull();
        assertThat(foundTodo.getTaskText()).isEqualTo("Test Task");
        assertThat(foundTodo.isDone()).isFalse();
        assertThat(foundTodo.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    public void testFindByUserId() {
        // Создаем несколько задач для пользователя
        Todo todo1 = new Todo("Task 1", false);
        todo1.setUser(testUser);
        todoRepository.save(todo1);

        Todo todo2 = new Todo("Task 2", true);
        todo2.setUser(testUser);
        todoRepository.save(todo2);

        // Получаем задачи пользователя
        List<Todo> userTodos = todoRepository.findByUserId(testUser.getId());

        // Проверяем, что обе задачи найдены
        assertThat(userTodos).hasSize(2);
        assertThat(userTodos).extracting(Todo::getTaskText)
                .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    public void testDeleteTodo() {
        // Создаем задачу
        Todo todo = new Todo("Delete Task", false);
        todo.setUser(testUser);
        Todo savedTodo = todoRepository.save(todo);

        // Удаляем задачу
        todoRepository.deleteById(savedTodo.getId());

        // Проверяем, что задача удалена
        assertThat(todoRepository.findById(savedTodo.getId())).isEmpty();
    }

    @Test
    public void testUpdateTodo() {
        // Создаем задачу
        Todo todo = new Todo("Update Task", false);
        todo.setUser(testUser);
        Todo savedTodo = todoRepository.save(todo);

        // Обновляем задачу
        savedTodo.setTaskText("Updated Task");
        savedTodo.setDone(true);
        todoRepository.save(savedTodo);

        // Проверяем, что задача обновлена
        Todo updatedTodo = todoRepository.findById(savedTodo.getId()).orElse(null);
        assertThat(updatedTodo).isNotNull();
        assertThat(updatedTodo.getTaskText()).isEqualTo("Updated Task");
        assertThat(updatedTodo.isDone()).isTrue();
    }
}
