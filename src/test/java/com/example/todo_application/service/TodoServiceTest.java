package com.example.todo_application.service;

import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import com.example.todo_application.repository.TodoRepository;
import com.example.todo_application.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoService todoService;

    private User testUser;
    private Todo testTodo;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);

        testTodo = new Todo("Test task", false);
        testTodo.setId(1L);
        testTodo.setUser(testUser);
    }

    @Test
    void testGetAllTodosByUserId() {
        Todo todo2 = new Todo("Second task", true);
        todo2.setId(2L);
        todo2.setUser(testUser);

        when(todoRepository.findByUserId(1L)).thenReturn(Arrays.asList(testTodo, todo2));

        List<Todo> todos = todoService.getAllTodosByUserId(1L);

        assertNotNull(todos);
        assertEquals(2, todos.size());
        verify(todoRepository).findByUserId(1L);
    }

    @Test
    void testCreateTodo() {
        Todo newTodo = new Todo("New task", false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo savedTodo = invocation.getArgument(0);
            savedTodo.setId(3L);
            return savedTodo;
        });

        Todo created = todoService.createTodo(1L, newTodo);

        assertNotNull(created);
        assertEquals(3L, created.getId());
        assertEquals("New task", created.getTaskText());
        assertEquals(testUser, created.getUser());
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void testCreateTodo_UserNotFound() {
        Todo newTodo = new Todo("New task", false);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                todoService.createTodo(999L, newTodo));

        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void testUpdateTodo() {
        Todo todoUpdate = new Todo("Updated task", true);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Todo updated = todoService.updateTodo(1L, 1L, todoUpdate);

        assertNotNull(updated);
        assertEquals("Updated task", updated.getTaskText());
        assertTrue(updated.isDone());
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void testUpdateTodo_TodoNotFound() {
        Todo todoUpdate = new Todo("Updated task", true);

        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                todoService.updateTodo(1L, 999L, todoUpdate));

        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void testUpdateTodo_TodoDoesNotBelongToUser() {
        User anotherUser = new User("another", "another@example.com", "password");
        anotherUser.setId(2L);

        Todo todoFromAnotherUser = new Todo("Another user's task", false);
        todoFromAnotherUser.setId(2L);
        todoFromAnotherUser.setUser(anotherUser);

        Todo todoUpdate = new Todo("Updated task", true);

        when(todoRepository.findById(2L)).thenReturn(Optional.of(todoFromAnotherUser));

        assertThrows(RuntimeException.class, () ->
                todoService.updateTodo(1L, 2L, todoUpdate));

        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    void testDeleteTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        doNothing().when(todoRepository).delete(any(Todo.class));

        todoService.deleteTodo(1L, 1L);

        verify(todoRepository).delete(testTodo);
    }

    @Test
    void testDeleteTodo_TodoNotFound() {
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                todoService.deleteTodo(1L, 999L));

        verify(todoRepository, never()).delete(any(Todo.class));
    }

    @Test
    void testDeleteTodo_TodoDoesNotBelongToUser() {
        User anotherUser = new User("another", "another@example.com", "password");
        anotherUser.setId(2L);

        Todo todoFromAnotherUser = new Todo("Another user's task", false);
        todoFromAnotherUser.setId(2L);
        todoFromAnotherUser.setUser(anotherUser);

        when(todoRepository.findById(2L)).thenReturn(Optional.of(todoFromAnotherUser));

        assertThrows(RuntimeException.class, () ->
                todoService.deleteTodo(1L, 2L));

        verify(todoRepository, never()).delete(any(Todo.class));
    }
}