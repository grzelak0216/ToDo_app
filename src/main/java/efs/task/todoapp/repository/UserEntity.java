package efs.task.todoapp.repository;

import efs.task.todoapp.util.ToDoException;
import efs.task.todoapp.web.HttpStatus;

public class UserEntity {
    private String username;
    private String password;

    public UserEntity() {}

    public UserEntity(String name, String pass) {
        setUsername(name);
        setPassword(pass);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (null == username || username.isBlank()) {
            throw new ToDoException(HttpStatus.BAD_REQUEST, "Błąd dodania użytkownika - pusta nazwa użytkownika");
        }
        this.username = username;
    }

    public String getPassword() { return password;
    }

    public void setPassword(String password) {
        if (null == password || password.isBlank()) {
            throw new ToDoException(HttpStatus.BAD_REQUEST, "Błąd dodania użytkownika - puste hasŁo");
        }
        this.password = password;
    }
}
