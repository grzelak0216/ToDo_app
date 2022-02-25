package efs.task.todoapp.repository;

import java.util.UUID;

import com.google.gson.Gson;
import efs.task.todoapp.util.ToDoException;
import efs.task.todoapp.web.HttpStatus;
import java.text.SimpleDateFormat;

public class TaskEntity {

    private UUID id;
    private transient String username;
    private String description;
    private String due;

    public TaskEntity() {}

    public TaskEntity(String desc, String due) {
        setDescription(desc);
        setDue(due);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (null == description || description.isBlank()) {
            throw new ToDoException(HttpStatus.BAD_REQUEST, "Błędne dane - pusty opis zadania");
        }
        this.description = description;
    }

    public String getDue() {
        return due;
    }

    public void setDue(String due) throws ToDoException {
        if (due != null && !due.isBlank()) {
            try {
                new SimpleDateFormat("yyyy-MM-dd").parse(due);
            }
            catch (Exception e) {
                throw new ToDoException(HttpStatus.BAD_REQUEST, "Niepoprawny format daty dla zadania!");
            }
        }
        this.due = due;
    }

    public void validateUser(String username) throws ToDoException {
        if (id == null) {
            throw new ToDoException(HttpStatus.BAD_REQUEST,
                "Nie można sprawdzić autora zadania, bo zadanie nie ma przypisanego ID.");
        }
        if (username == null || username.isBlank()) {
            throw new ToDoException(HttpStatus.FORBIDDEN,
                "Nie można sprawdzić autora zadania " + this.id.toString() + " - nie przekazano nazwy użytkownika.");
        }
        if (!username.equals(this.username)) {
            throw new ToDoException(HttpStatus.FORBIDDEN,
                "Użytkownik + `" + username + "` nie jest autorem zadania " + this.id.toString());
        }
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Override
    public int hashCode() {
        int result = 11;
        result = 31 * result + id.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + due.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (null != o) {
            if (this == o) {
                return true;
            }
            if (o instanceof TaskEntity) {
                final TaskEntity task2 = (TaskEntity) o;

                return id.equals(task2.getId())
                    && description.equals(task2.getDescription())
                    && due.equals(task2.getDue());
            }
        }
        return false;
    }
}
