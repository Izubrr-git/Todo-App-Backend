package com.example.todo_application.dto;

import com.example.todo_application.model.Todo;

public class TodoDTO {
    private Long todoId;
    private String taskText;
    private boolean isDone;
    private Long userId;
    private String username;

    // Constructor
    public TodoDTO() {
    }

    public TodoDTO(Todo todo) {
        this.todoId = todo.getId();
        this.taskText = todo.getTaskText();
        this.isDone = todo.isDone();

        // Getting user information if available
        if (todo.getUser() != null) {
            this.userId = todo.getUser().getId();
            this.username = todo.getUser().getUsername();
        }
    }

    // Getters
    public Long getTodoId() {
        return todoId;
    }

    public String getTaskText() {
        return taskText;
    }

    public boolean isDone() {
        return isDone;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    // Setters
    public void setTodoId(Long todoId) {
        this.todoId = todoId;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public void setDone(boolean done) {
        this.isDone = done;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // toString method
    @Override
    public String toString() {
        return "TodoDTO{" +
                "id=" + todoId +
                ", taskText='" + taskText + '\'' +
                ", isDone=" + isDone +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                '}';
    }

    // DTO to entity conversion
    public Todo toEntity() {
        Todo todo = new Todo();
        todo.setId(this.todoId);
        todo.setTaskText(this.taskText);
        todo.setDone(this.isDone);
        return todo;
    }
}