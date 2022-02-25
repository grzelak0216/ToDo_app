package efs.task.todoapp.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import efs.task.todoapp.ToDoApplication;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.service.ToDoService;
import efs.task.todoapp.util.ToDoException;
import efs.task.todoapp.util.ToDoHandlersUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

public class HttpUserHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(HttpUserHandler.class.getName());

    private ToDoService service;

    public HttpUserHandler(ToDoService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        HttpStatus status = HttpStatus.OK;

        System.out.println("-".repeat(64));

        try {
            if (httpExchange.getRequestMethod().equals("POST")) {

                final Map<String, String> userData = ToDoHandlersUtils.readRequestBody(httpExchange);
                UserEntity userToAdd = new UserEntity();
                userToAdd.setUsername(userData.get("username"));
                userToAdd.setPassword(userData.get("password"));

                service.addUser(userToAdd);
                status = HttpStatus.CREATED;
            } else {
                throw new ToDoException(HttpStatus.METHOD_NOT_ALLOWED, "Only \"POST\" method is allowed!");
            }
        } catch (ToDoException e) {
            status = e.getHttpStatus();
            LOGGER.info("User Handler, exception: " + e.getMessage());
        }

        LOGGER.info("User Handler, response: " + status.debugStatus());

        byte[] response = status.htmlBody().getBytes();
        httpExchange.sendResponseHeaders(status.getCode(), response.length);

        final OutputStream responseBody = httpExchange.getResponseBody();
        responseBody.write(response);

        httpExchange.close();

        System.out.println("-".repeat(64));
    }
}
