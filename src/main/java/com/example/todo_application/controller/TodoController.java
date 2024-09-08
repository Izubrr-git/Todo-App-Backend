package com.example.todo_application.controller;

import com.example.todo_application.model.Todo;
import com.example.todo_application.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/todos")
public class TodoController {
    @Autowired
    private TodoService todoService;

    @GetMapping
    public List<Todo> getAllTodosByUserId(@PathVariable Long userId) {
        return todoService.getAllTodosByUserId(userId);
    }

    @PostMapping
    public Todo createTodo(@PathVariable Long userId, @RequestBody Todo todo) {
        return todoService.createTodo(userId, todo);
    }

    @PutMapping("/{todoId}")
    public Todo updateTodo(@PathVariable Long userId, @PathVariable Long todoId, @RequestBody Todo todoDetails) {
        return todoService.updateTodo(userId, todoId, todoDetails);
    }

    @DeleteMapping("/{todoId}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long userId, @PathVariable Long todoId) {
        todoService.deleteTodo(userId, todoId);
        return ResponseEntity.ok().build();
    }
}