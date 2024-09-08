package com.example.todo_application.model;

import jakarta.persistence.*;

@Entity
@Table(name = "todos")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String taskText;
    private boolean isDone;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructor
    public Todo() {}

    public Todo(String taskText, boolean isDone) {
        this.taskText = taskText;
        this.isDone = isDone;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTaskText() {
        return taskText;
    }

    public boolean isDone() {
        return isDone;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    // toString method
    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", taskText='" + taskText + '\'' +
                ", isDone=" + isDone +
                '}';
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}