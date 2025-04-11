package com.example.todo_application.data_transfer_object;

import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDTOTest {

    @Test
    void testUserToDTO() {
        // Prepare test data
        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);
        user.setAvatarUrl("avatar.jpg");

        Todo todo1 = new Todo("Task 1", false);
        todo1.setId(1L);
        todo1.setUser(user);

        Todo todo2 = new Todo("Task 2", true);
        todo2.setId(2L);
        todo2.setUser(user);

        user.setTodos(Arrays.asList(todo1, todo2));

        // User to UserDTO conversion
         UserDTO userDTO = new UserDTO(user);

        // Result verification
        assertEquals(1L, userDTO.getId());
        assertEquals("testuser", userDTO.getUsername());
        assertEquals("test@example.com", userDTO.getEmail());
        assertEquals("avatar.jpg", userDTO.getAvatarUrl());

        // Check todos List
        assertNotNull(userDTO.getTodos());
        assertEquals(2, userDTO.getTodos().size());
        assertEquals("Task 1", userDTO.getTodos().get(0).getTaskText());
        assertEquals("Task 2", userDTO.getTodos().get(1).getTaskText());
    }

    @Test
    void testUserDTOToEntity() {
        // Prepare test data
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setAvatarUrl("avatar.jpg");

        TodoDTO todoDTO1 = new TodoDTO();
        todoDTO1.setId(1L);
        todoDTO1.setTaskText("Task 1");
        todoDTO1.setDone(false);
        todoDTO1.setUserId(1L);

        TodoDTO todoDTO2 = new TodoDTO();
        todoDTO2.setId(2L);
        todoDTO2.setTaskText("Task 2");
        todoDTO2.setDone(true);
        todoDTO2.setUserId(1L);

        userDTO.setTodos(Arrays.asList(todoDTO1, todoDTO2));

        // UserDTO to User conversion
        User user = userDTO.toEntity();

        // Result verification
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("avatar.jpg", user.getAvatarUrl());

        // Converting from DTO to Entity doesn't set task list
        assertNull(user.getTodos());
    }

    @Test
    void testUserToDTO_WithNullTodos() {
        // Prepare test data
        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);
        user.setAvatarUrl("avatar.jpg");
        user.setTodos(null);

        // User to UserDTO conversion
        UserDTO userDTO = new UserDTO(user);

        // Result verification
        assertEquals(1L, userDTO.getId());
        assertEquals("testuser", userDTO.getUsername());
        assertEquals("test@example.com", userDTO.getEmail());
        assertEquals("avatar.jpg", userDTO.getAvatarUrl());

        // Check task list - must be empty but not null
        assertNotNull(userDTO.getTodos());
        assertTrue(userDTO.getTodos().isEmpty());
    }

    @Test
    void testUserDTONoArgsConstructor() {
        // Checking the NoArgs constructor's operation
        UserDTO userDTO = new UserDTO();

        assertNull(userDTO.getId());
        assertNull(userDTO.getUsername());
        assertNull(userDTO.getEmail());
        assertNull(userDTO.getAvatarUrl());
        assertNull(userDTO.getTodos());
    }
}