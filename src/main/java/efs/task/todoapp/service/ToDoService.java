package efs.task.todoapp.service;

import efs.task.todoapp.ToDoApplication;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UserRepository;

import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.logging.Logger;

import efs.task.todoapp.web.HttpStatus;
import efs.task.todoapp.util.ToDoException;

public class ToDoService {

    private static final Logger LOGGER = Logger.getLogger(ToDoService.class.getName());

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ToDoService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public String addUser(UserEntity userEntity) throws ToDoException {
        String userId = userRepository.save(userEntity);
        final String username = userEntity.getUsername();
        if (null == userId) {
            throw new ToDoException(HttpStatus.CONFLICT, "błąd dodania - użytkownik \"" + username + "\" już istnieje");
        }
        LOGGER.info("dodano pomyślnie nowego użytkownika \"" + username + "\".");
        return userId;
    }

    public UserEntity findUser(String username) {
        return userRepository.query(username);
    }

    public UUID addTask(TaskEntity taskEntity, String authorUsername) throws ToDoException {
        taskEntity.setUsername(authorUsername);
        UUID taskId = taskRepository.save(taskEntity);

        LOGGER.info("dodano pomyślnie nowe zadanie, ID: " + taskId.toString() + ".");
        return taskId;
    }

    public TaskEntity findTask(UUID id) {
        return taskRepository.query(id);
    }

    public List<TaskEntity> findTasksOfUser(String username) {
        return taskRepository.query(t -> t.getUsername().equals(username));
    }

    public TaskEntity updateTask(UUID id, TaskEntity updatedTask) {
        return taskRepository.update(id, updatedTask);
    }

    public void removeTask(UUID id) {
        taskRepository.delete(id);
    }
}
