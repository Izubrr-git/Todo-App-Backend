package com.example.todo_application.controller;

import com.example.todo_application.dto.TodoDTO;
import com.example.todo_application.model.Todo;
import com.example.todo_application.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/todos")
public class TodoController {
    @Autowired
    private TodoService todoService;

    @GetMapping
    public List<TodoDTO> getAllTodosByUserId(@PathVariable Long userId) {
        return todoService.getAllTodosByUserId(userId).stream()
                .map(TodoDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping
    public TodoDTO createTodo(@PathVariable Long userId, @RequestBody TodoDTO todoDTO) {
        Todo todo = todoDTO.toEntity();
        return new TodoDTO(todoService.createTodo(userId, todo));
    }

    @PutMapping("/{todoId}")
    public TodoDTO updateTodo(@PathVariable Long userId, @PathVariable Long todoId, @RequestBody TodoDTO todoDTO) {
        Todo todoDetails = todoDTO.toEntity();
        return new TodoDTO(todoService.updateTodo(userId, todoId, todoDetails));
    }

    @DeleteMapping("/{todoId}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long userId, @PathVariable Long todoId) {
        todoService.deleteTodo(userId, todoId);
        return ResponseEntity.ok().build();
    }
}