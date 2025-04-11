package com.example.todo_application.data_transfer_object;

import com.example.todo_application.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private List<TodoDTO> todos;

    // Constructor
    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.avatarUrl = user.getAvatarUrl();

        // CommonTodo list to TodoDTO list conversion
        if (user.getTodos() != null) {
            this.todos = user.getTodos().stream()
                    .map(TodoDTO::new)
                    .collect(Collectors.toList());
        } else {
            this.todos = new ArrayList<>();
        }
    }

    // DTO to entity conversion
    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setAvatarUrl(this.avatarUrl);

        return user;
    }
}