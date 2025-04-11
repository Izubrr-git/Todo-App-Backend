package com.example.todo_application;

import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import com.example.todo_application.repository.TodoRepository;
import com.example.todo_application.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TodoApplicationIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoRepository todoRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void testUserRepositoryCrud() {
        // Create user
        User user = new User("integrationtest", "integration@test.com", passwordEncoder.encode("password"));
        User savedUser = userRepository.save(user);

        // Checking that the user was saved
        assertNotNull(savedUser.getId());

        // Search user by id
        Optional<User> foundById = userRepository.findById(savedUser.getId());
        assertTrue(foundById.isPresent());
        assertEquals("integrationtest", foundById.get().getUsername());

        // Search user by email
        Optional<User> foundByEmail = userRepository.findByEmail("integration@test.com");
        assertTrue(foundByEmail.isPresent());

        // Search user by username
        Optional<User> foundByUsername = userRepository.findByUsername("integrationtest");
        assertTrue(foundByUsername.isPresent());

        // Update user
        User toUpdate = foundById.get();
        toUpdate.setUsername("updated");
        User updated = userRepository.save(toUpdate);
        assertEquals("updated", updated.getUsername());

        // Delete user
        userRepository.delete(updated);
        Optional<User> afterDelete = userRepository.findById(updated.getId());
        assertFalse(afterDelete.isPresent());
    }

    @Test
    void testTodoRepositoryCrud() {
        // Create user
        User user = new User("todoUser", "todo@test.com", passwordEncoder.encode("password"));
        User savedUser = userRepository.save(user);

        // Create task
        Todo todo = new Todo("Integration test task", false);
        todo.setUser(savedUser);
        Todo savedTodo = todoRepository.save(todo);

        // Checking that the task was saved
        assertNotNull(savedTodo.getId());

        // Search task by id
        Optional<Todo> foundById = todoRepository.findById(savedTodo.getId());
        assertTrue(foundById.isPresent());
        assertEquals("Integration test task", foundById.get().getTaskText());

        // Search task by userId
        List<Todo> foundByUserId = todoRepository.findByUserId(savedUser.getId());
        assertFalse(foundByUserId.isEmpty());
        assertEquals(1, foundByUserId.size());

        // Update task
        Todo toUpdate = foundById.get();
        toUpdate.setTaskText("Updated task");
        toUpdate.setDone(true);
        Todo updated = todoRepository.save(toUpdate);
        assertEquals("Updated task", updated.getTaskText());
        assertTrue(updated.isDone());

        // Delete task
        todoRepository.delete(updated);
        Optional<Todo> afterDelete = todoRepository.findById(updated.getId());
        assertFalse(afterDelete.isPresent());
    }

    @Test
    void testUserTodoRelationship() {
        // Create user
        User user = new User("relationUser", "relation@test.com", passwordEncoder.encode("password"));
        User savedUser = userRepository.save(user);

        // Create several tasks for the user
        Todo todo1 = new Todo("First task", false);
        todo1.setUser(savedUser);
        todoRepository.save(todo1);

        Todo todo2 = new Todo("Second task", true);
        todo2.setUser(savedUser);
        todoRepository.save(todo2);

        // Checking one-to-many relationship
        List<Todo> userTodos = todoRepository.findByUserId(savedUser.getId());
        assertEquals(2, userTodos.size());

        // Checking cascade deletion
        userRepository.delete(savedUser);
        List<Todo> todosAfterUserDelete = todoRepository.findByUserId(savedUser.getId());
        assertTrue(todosAfterUserDelete.isEmpty());
    }
}