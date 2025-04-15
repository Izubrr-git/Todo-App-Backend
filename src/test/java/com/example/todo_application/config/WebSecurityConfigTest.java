package com.example.todo_application.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testApiEndpointsAreAccessible() throws Exception {
        // Проверка доступности API без аутентификации
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        // Проверка доступности точки регистрации
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCsrfIsDisabled() throws Exception {
        // Проверка, что POST запросы работают без CSRF токена
        // (если CSRF включен, то без токена запрос бы не прошел)
        mockMvc.perform(post("/api/users/register"))
                .andExpect(status().isBadRequest()); // Ожидаем BadRequest из-за пустого тела, но не 403 Forbidden
    }

    @Test
    public void testNonApiEndpointsRequireAuthentication() throws Exception {
        // Проверка, что не-API эндпоинты требуют аутентификации
        mockMvc.perform(get("/non-api-path"))
                .andExpect(status().isUnauthorized());
    }
}