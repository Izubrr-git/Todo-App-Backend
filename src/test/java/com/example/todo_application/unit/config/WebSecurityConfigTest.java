package com.example.todo_application.unit.config;

import com.example.todo_application.config.WebSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(WebSecurityConfig.class)
public class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testApiEndpointsAreAccessible() throws Exception {
        // Проверяем, что запросы к API проходят через фильтры безопасности
        // и не блокируются (не возвращают 403 Forbidden)
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isNotFound()); // 404 потому что контроллер не загружен

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound()); // 404 потому что контроллер не загружен
    }

    @Test
    public void testCsrfIsDisabled() throws Exception {
        // Проверка, что POST запросы работают без CSRF токена
        mockMvc.perform(post("/api/users/register"))
                .andExpect(status().isNotFound()); // 404 потому что контроллер не загружен

        // Даже если добавим CSRF токен, результат тот же
        mockMvc.perform(post("/api/users/register").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testNonApiEndpointsRequireAuthentication() throws Exception {
        // Проверка, что не-API эндпоинты требуют аутентификации
        mockMvc.perform(get("/non-api-path"))
                .andExpect(status().isUnauthorized()); // 401 - требуется аутентификация
    }

    @Test
    @WithMockUser
    public void testNonApiEndpointsAccessibleWithAuth() throws Exception {
        // Проверка, что не-API эндпоинты доступны с аутентификацией
        mockMvc.perform(get("/non-api-path"))
                .andExpect(status().isNotFound()); // 404 потому что endpoint не существует
    }

    @Test
    public void testInvalidTokenGives401() throws Exception {
        // Проверка, что неверный токен дает 401
        mockMvc.perform(get("/non-api-path")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCsrfProtectionOnNonApiEndpoints() throws Exception {
        // Проверка, что CSRF защита работает на не-API эндпоинтах
        mockMvc.perform(post("/some-form-submit"))
                .andExpect(status().isForbidden()); // 403 - нет CSRF токена
    }
}