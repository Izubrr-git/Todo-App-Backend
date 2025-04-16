package com.example.todo_application.unit.service;

import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import com.example.todo_application.repository.TodoRepository;
import com.example.todo_application.repository.UserRepository;
import com.example.todo_application.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
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
    public void setup() {
        // Создаем тестового пользователя
        testUser = new User("todoUser", "todo@example.com", "password");
        testUser.setId(1L);

        // Создаем тестовую задачу
        testTodo = new Todo("Test Task", false);
        testTodo.setId(1L);
        testTodo.setUser(testUser);

        // Добавляем задачу в список задач пользователя
        testUser.getTodos().add(testTodo);
    }

    @Test
    public void testGetAllTodosByUserId() {
        Todo todo2 = new Todo("Second Task", true);
        todo2.setId(2L);
        todo2.setUser(testUser);

        when(todoRepository.findByUserId(1L)).thenReturn(Arrays.asList(testTodo, todo2));

        List<Todo> todos = todoService.getAllTodosByUserId(1L);

        assertThat(todos).hasSize(2);
        assertThat(todos).extracting(Todo::getTaskText)
                .containsExactly("Test Task", "Second Task");
    }

    @Test
    public void testCreateTodo() {
        Todo newTodo = new Todo("New Task", false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo savedTodo = invocation.getArgument(0);
            savedTodo.setId(2L);
            return savedTodo;
        });

        Todo createdTodo = todoService.createTodo(1L, newTodo);

        assertThat(createdTodo).isNotNull();
        assertThat(createdTodo.getId()).isEqualTo(2L);
        assertThat(createdTodo.getTaskText()).isEqualTo("New Task");
        assertThat(createdTodo.getUser()).isEqualTo(testUser);
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    public void testCreateTodo_UserNotFound() {
        Todo newTodo = new Todo("New Task", false);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> todoService.createTodo(999L, newTodo));
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    public void testUpdateTodo() {
        Todo todoDetails = new Todo("Updated Task", true);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Todo updatedTodo = todoService.updateTodo(1L, 1L, todoDetails);

        assertThat(updatedTodo).isNotNull();
        assertThat(updatedTodo.getTaskText()).isEqualTo("Updated Task");
        assertThat(updatedTodo.isDone()).isTrue();
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    public void testUpdateTodo_TodoNotFound() {
        Todo todoDetails = new Todo("Updated Task", true);

        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> todoService.updateTodo(1L, 999L, todoDetails));
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    public void testUpdateTodo_TodoNotBelongToUser() {
        User anotherUser = new User("anotherUser", "another@example.com", "password");
        anotherUser.setId(2L);

        Todo todoDetails = new Todo("Updated Task", true);

        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        assertThrows(RuntimeException.class, () -> todoService.updateTodo(2L, 1L, todoDetails));
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    public void testDeleteTodo() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        todoService.deleteTodo(1L, 1L);

        verify(todoRepository).delete(testTodo);
    }

    @Test
    public void testDeleteTodo_TodoNotFound() {
        when(todoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> todoService.deleteTodo(1L, 999L));
        verify(todoRepository, never()).delete(any(Todo.class));
    }

    @Test
    public void testDeleteTodo_TodoNotBelongToUser() {
        when(todoRepository.findById(1L)).thenReturn(Optional.of(testTodo));

        assertThrows(RuntimeException.class, () -> todoService.deleteTodo(2L, 1L));
        verify(todoRepository, never()).delete(any(Todo.class));
    }
}