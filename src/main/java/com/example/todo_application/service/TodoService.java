package com.example.todo_application.service;

import com.example.todo_application.model.Todo;
import com.example.todo_application.model.User;
import com.example.todo_application.repository.TodoRepository;
import com.example.todo_application.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {
    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Todo> getAllTodosByUserId(Long userId) {
        return todoRepository.findByUserId(userId);
    }

    public Todo createTodo(Long userId, Todo todo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        todo.setUser(user);
        return todoRepository.save(todo);
    }

    public Todo updateTodo(Long userId, Long todoId, Todo todoDetails) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found with id " + todoId));

        if (!todo.getUser().getId().equals(userId)) {
            throw new RuntimeException("Todo does not belong to user");
        }

        todo.setTaskText(todoDetails.getTaskText());
        todo.setDone(todoDetails.isDone());

        return todoRepository.save(todo);
    }

    public void deleteTodo(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found with id " + todoId));

        if (!todo.getUser().getId().equals(userId)) {
            throw new RuntimeException("Todo does not belong to user");
        }

        todoRepository.delete(todo);
    }
}