package com.example.todo_application.data_transfer_object;

import com.example.todo_application.model.Todo;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class TodoDTO {
    private Long id;
    private String taskText;
    private boolean done;
    private Long userId;
    private String username;

    // Constructor
    public TodoDTO(Todo todo) {
        this.id = todo.getId();
        this.taskText = todo.getTaskText();
        this.done = todo.isDone();

        // Getting user information if available
        if (todo.getUser() != null) {
            this.userId = todo.getUser().getId();
            this.username = todo.getUser().getUsername();
        }
    }

    // DTO to entity conversion
    public Todo toEntity() {
        Todo todo = new Todo();
        todo.setId(this.id);
        todo.setTaskText(this.taskText);
        todo.setDone(this.done);
        return todo;
    }
}