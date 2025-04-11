package com.example.todo_application.data_transfer_object;

import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TodoDTOTest {

    @Test
    void testTodoToDTO() {
        // Prepare test data
        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);

        Todo todo = new Todo("Test task", false);
        todo.setId(1L);
        todo.setUser(user);

        // CommonTodo to TodoDTO conversion
        TodoDTO todoDTO = new TodoDTO(todo);

        // Result verification
        assertEquals(1L, todoDTO.getId());
        assertEquals("Test task", todoDTO.getTaskText());
        assertFalse(todoDTO.isDone());
        assertEquals(1L, todoDTO.getUserId());
        assertEquals("testuser", todoDTO.getUsername());
    }

    @Test
    void testTodoDTOToEntity() {
        // Prepare test data
        TodoDTO todoDTO = new TodoDTO();
        todoDTO.setId(1L);
        todoDTO.setTaskText("Test task");
        todoDTO.setDone(true);
        todoDTO.setUserId(1L);
        todoDTO.setUsername("testuser");

        // CommonTodo to TodoDTO conversion
        Todo todo = todoDTO.toEntity();

        // Result verification
        assertEquals(1L, todo.getId());
        assertEquals("Test task", todo.getTaskText());
        assertTrue(todo.isDone());
        // Note: user is not selected when converting from DTO to Entity
        assertNull(todo.getUser());
    }

    @Test
    void testTodoDTOWithNullUser() {
        // Подготовка данных
        Todo todo = new Todo("Test task", false);
        todo.setId(1L);
        todo.setUser(null);

        // Конвертирование Todo в TodoDTO
        TodoDTO todoDTO = new TodoDTO(todo);

        // Проверка результатов
        assertEquals(1L, todoDTO.getId());
        assertEquals("Test task", todoDTO.getTaskText());
        assertFalse(todoDTO.isDone());
        assertNull(todoDTO.getUserId());
        assertNull(todoDTO.getUsername());
    }
}