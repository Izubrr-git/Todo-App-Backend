package com.example.todo_application.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.todo_application.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalExceptionHandler.class)
@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Test
    public void testHandleRuntimeException() throws Exception {
        // Настройка имитации исключения в сервисе
        String errorMessage = "Test runtime exception";
        when(userService.getUserById(anyLong())).thenThrow(new RuntimeException(errorMessage));

        // Выполнение запроса, который вызовет исключение
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }
}