package com.example.todo_application.controller;

import com.example.todo_application.data_transfer_object.TodoDTO;
import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import com.example.todo_application.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TodoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    private User testUser;
    private Todo testTodo;
    private TodoDTO todoDTO;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);

        testTodo = new Todo("Test task", false);
        testTodo.setId(1L);
        testTodo.setUser(testUser);

        todoDTO = new TodoDTO(testTodo);
    }

    @Test
    void testGetAllTodosByUserId() throws Exception {
        Todo todo2 = new Todo("Second task", true);
        todo2.setId(2L);
        todo2.setUser(testUser);

        List<Todo> todos = Arrays.asList(testTodo, todo2);

        when(todoService.getAllTodosByUserId(1L)).thenReturn(todos);

        mockMvc.perform(get("/api/users/1/todos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].taskText").value("Test task"))
                .andExpect(jsonPath("$[0].done").value(false))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].taskText").value("Second task"))
                .andExpect(jsonPath("$[1].done").value(true));
    }

    @Test
    void testCreateTodo() throws Exception {
        TodoDTO newTodoDTO = new TodoDTO();
        newTodoDTO.setTaskText("New task");
        newTodoDTO.setDone(false);

        Todo newTodo = new Todo("New task", false);
        newTodo.setId(3L);
        newTodo.setUser(testUser);

        when(todoService.createTodo(anyLong(), any(Todo.class))).thenReturn(newTodo);

        mockMvc.perform(post("/api/users/1/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTodoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.taskText").value("New task"))
                .andExpect(jsonPath("$.done").value(false))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void testUpdateTodo() throws Exception {
        TodoDTO updateDTO = new TodoDTO();
        updateDTO.setTaskText("Updated task");
        updateDTO.setDone(true);

        Todo updatedTodo = new Todo("Updated task", true);
        updatedTodo.setId(1L);
        updatedTodo.setUser(testUser);

        when(todoService.updateTodo(anyLong(), anyLong(), any(Todo.class))).thenReturn(updatedTodo);

        mockMvc.perform(put("/api/users/1/todos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.taskText").value("Updated task"))
                .andExpect(jsonPath("$.done").value(true))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void testDeleteTodo() throws Exception {
        doNothing().when(todoService).deleteTodo(1L, 1L);

        mockMvc.perform(delete("/api/users/1/todos/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}