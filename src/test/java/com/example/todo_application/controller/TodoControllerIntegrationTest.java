package com.example.todo_application.controller;

import com.example.todo_application.dto.TodoDTO;
import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import com.example.todo_application.repository.TodoRepository;
import com.example.todo_application.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TodoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private Todo testTodo;

    @BeforeEach
    public void setup() {
        // Создаем тестового пользователя
        testUser = new User("todoUser", "todo@example.com", "password123");
        testUser = userRepository.save(testUser);

        // Создаем тестовую задачу
        testTodo = new Todo("Test Task", false);
        testTodo.setUser(testUser);
        testTodo = todoRepository.save(testTodo);
    }

    @AfterEach
    public void cleanup() {
        todoRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testGetAllTodosByUserId() throws Exception {
        // Создаем вторую задачу
        Todo todo2 = new Todo("Second Task", true);
        todo2.setUser(testUser);
        todoRepository.save(todo2);

        mockMvc.perform(get("/api/users/{userId}/todos", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].taskText").value("Test Task"))
                .andExpect(jsonPath("$[1].taskText").value("Second Task"));
    }

    @Test
    public void testCreateTodo() throws Exception {
        TodoDTO todoDTO = new TodoDTO();
        todoDTO.setTaskText("New Task");
        todoDTO.setDone(false);

        mockMvc.perform(post("/api/users/{userId}/todos", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskText").value("New Task"))
                .andExpect(jsonPath("$.done").value(false))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));

        // Проверка, что задача создана в базе данных
        assertThat(todoRepository.findByUserId(testUser.getId()).size()).isEqualTo(2);
    }

    @Test
    public void testUpdateTodo() throws Exception {
        TodoDTO todoDTO = new TodoDTO();
        todoDTO.setTaskText("Updated Task");
        todoDTO.setDone(true);

        mockMvc.perform(put("/api/users/{userId}/todos/{todoId}", testUser.getId(), testTodo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskText").value("Updated Task"))
                .andExpect(jsonPath("$.done").value(true));

        // Проверка, что задача обновлена в базе данных
        Todo updatedTodo = todoRepository.findById(testTodo.getId()).orElse(null);
        assertThat(updatedTodo).isNotNull();
        assertThat(updatedTodo.getTaskText()).isEqualTo("Updated Task");
        assertThat(updatedTodo.isDone()).isTrue();
    }

    @Test
    public void testUpdateTodo_TodoNotFound() throws Exception {
        TodoDTO todoDTO = new TodoDTO();
        todoDTO.setTaskText("Updated Task");
        todoDTO.setDone(true);

        mockMvc.perform(put("/api/users/{userId}/todos/{todoId}", testUser.getId(), 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Todo not found with id 999"));
    }

    @Test
    public void testUpdateTodo_TodoNotBelongToUser() throws Exception {
        // Создаем другого пользователя
        User anotherUser = new User("anotherUser", "another@example.com", "password");
        anotherUser = userRepository.save(anotherUser);

        TodoDTO todoDTO = new TodoDTO();
        todoDTO.setTaskText("Updated Task");
        todoDTO.setDone(true);

        mockMvc.perform(put("/api/users/{userId}/todos/{todoId}", anotherUser.getId(), testTodo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Todo does not belong to user"));
    }

    @Test
    public void testDeleteTodo() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}/todos/{todoId}", testUser.getId(), testTodo.getId()))
                .andExpect(status().isOk());

        // Проверка, что задача удалена из базы данных
        assertThat(todoRepository.findById(testTodo.getId())).isEmpty();
    }

    @Test
    public void testDeleteTodo_TodoNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}/todos/{todoId}", testUser.getId(), 999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Todo not found with id 999"));
    }

    @Test
    public void testDeleteTodo_TodoNotBelongToUser() throws Exception {
        // Создаем другого пользователя
        User anotherUser = new User("anotherUser", "another@example.com", "password");
        anotherUser = userRepository.save(anotherUser);

        mockMvc.perform(delete("/api/users/{userId}/todos/{todoId}", anotherUser.getId(), testTodo.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Todo does not belong to user"));
    }
}