package efs.task.todoapp.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import efs.task.todoapp.Base64Utils;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.service.ToDoService;
import efs.task.todoapp.web.WebServerFactory;

public class ToDoTestUtils {

    public static final Gson gson = new Gson();

    public static String generateBodyFromUserData(String username, String password) {
        final Map<String, String> jsonData = new HashMap<>();

        if (username != null)
            jsonData.put("username", username);

        if (password != null)
            jsonData.put("password", password);

        return gson.toJson(jsonData);
    }

    public static String generateBodyFromTaskData(String description, String due) {
        final Map<String, String> jsonData = new HashMap<>();

        if (description != null)
            jsonData.put("description", description);

        if (due != null)
            jsonData.put("due", due);

        return gson.toJson(jsonData);
    }

    public static UserEntity addUser(String username, String password) {
        final ToDoService service = WebServerFactory.lastService;
        final UserEntity user = new UserEntity(username, password);
        service.addUser(user);
        return user;
    }

    public static TaskEntity addTask(String description, String due, String username) {
        final ToDoService service = WebServerFactory.lastService;
        final TaskEntity task = new TaskEntity(description, due);
        service.addTask(task, username);
        return task;
    }

    public static String userDataToAuth(String name, String pass) {
        return Base64Utils.encode(name) + ":" + Base64Utils.encode(pass);
    }

}
