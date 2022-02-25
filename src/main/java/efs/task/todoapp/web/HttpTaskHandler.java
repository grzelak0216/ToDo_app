package efs.task.todoapp.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import efs.task.todoapp.Base64Utils;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class HttpTaskHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(HttpTaskHandler.class.getName());

    private ToDoService service;

    public HttpTaskHandler(ToDoService service) {
        this.service = service;
    }

    private String[] userDataFromAuthHeader(String auth) throws ToDoException {
        // pozyskanie danych użytkownika z nagłóweka (dekodowanie metodą "Base64")
        // oraz sprawdzenie, czy nazwa i hasło nie są puste

        String[] authParts = auth.split(":");

        if (authParts.length != 2) {
            throw new ToDoException(HttpStatus.BAD_REQUEST, "Nagłówek \"auth\" powinien zawierać dwie części oddzielone dwukropkiem.");
        }

        String[] result = new String[2];
        String[] exceptionNames = {"nazwy użytkownika", "hasła"};

        for (int i = 0; i < 2; i++) {
            try {
                if (authParts[i] == null || authParts[i].isBlank()) {
                    throw new Exception();
                }

                result[i] = Base64Utils.decode(authParts[i]);

            } catch (Exception e) {
                throw new ToDoException(HttpStatus.BAD_REQUEST,
                    "Niepoprawny nagłówek Auth. Nie udało się zdekodować" + exceptionNames[i] + ".");
            }
        }

        return result;
    }

    private String[] checkAuthHeader(Headers headers) {
        // jeśli nagłówek AUTH istnieje i jest poprawny,
        // to zostaną zwrócone zdekodowane dane (tablica dwóch Stringów)

        String auth;

        try {
            if (null == headers) {
                throw new Exception();
            }

            List<String> headerList = headers.get("auth");
            if (null == headerList || headerList.size() < 1) {
                throw new Exception();
            }

            auth = headerList.get(0);

            if (null == auth || auth.isBlank()) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new ToDoException(HttpStatus.BAD_REQUEST, "Brakuje nagłówka \"auth\"!");
        }

        LOGGER.info("Task Handler, auth: `" + auth + "`");

        return userDataFromAuthHeader(auth);
    }

    private UserEntity findAndValidateUser(String[] userData) throws ToDoException {
        // Sprawdzamy czy istnieje użytkownik o podanym loginie oraz czy ma poprawne hasło

        final String name = userData[0];
        final String pass = userData[1];

        UserEntity user = service.findUser(name);

        if (null == user) {
            throw new ToDoException(HttpStatus.UNAUTHORIZED, "Użytkownik `" + name + "` nie istnieje!");
        }

        if (!user.getPassword().equals(pass)) {
            throw new ToDoException(HttpStatus.UNAUTHORIZED, "Niepoprawne hasło dla użytkownika `" + name + "`!");
        }

        LOGGER.info("Task Handler, login user: `" + user.getUsername() + "`");
        return user;
    }

    private UUID getTaskIdFromAddress(String address, boolean canBeEmpty) throws ToDoException {
        // Sprawdzenie czy istnieje ID zadania i czy jest poprawne

        try {
            final int slashPos = "/todo/task".length();
            if (address.length() < (slashPos + 1) || '/' != address.charAt(slashPos)) {
                if (canBeEmpty) {
                    return null;
                }
                throw new Exception();
            }
            final String taskId = address.substring(slashPos + 1);
            if (null == taskId || taskId.isBlank()) {
                if (canBeEmpty) {
                    return null;
                }
                throw new Exception();
            }

            LOGGER.info("Task Handler, canBeEmpty: " + canBeEmpty + ", ID parameter: `" + taskId + "`");
            return UUID.fromString(taskId);

        } catch (Exception e) {
            throw new ToDoException(HttpStatus.BAD_REQUEST, "Brak ID zadania lub ID zadania nie jest w formacie UUID.");
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        HttpStatus status = HttpStatus.OK;
        byte[] response = new byte[0];

        System.out.println("-".repeat(64));

        try {
            // Czy header istnieje i jest poprawny?
            String[] userData = checkAuthHeader(httpExchange.getRequestHeaders());

            switch (httpExchange.getRequestMethod()) {
                case "POST": {
                    // dodanie nowego zadania

                    LOGGER.info("Task Handler [POST]");

                    // Czy request body istnieje i zawiera poprawne dane?
                    final Map<String, String> taskData = ToDoHandlersUtils.readRequestBody(httpExchange);
                    TaskEntity taskToAdd = new TaskEntity();
                    taskToAdd.setDescription(taskData.get("description"));
                    taskToAdd.setDue(taskData.get("due"));

                    // Czy uźytkownik o podanym loginie istnieje?
                    final UserEntity user = findAndValidateUser(userData);

                    UUID taskId = service.addTask(taskToAdd, user.getUsername());
                    response = ("{\"id\":\"" + taskId.toString() + "\"}").getBytes();
                    status = HttpStatus.CREATED;
                }
                break;

                case "GET": {
                    // Odczytanie zadań użytkownika

                    LOGGER.info("Task Handler [GET]");

                    // Czy przekazano ID zadania i czy jest ono dobre?
                    final UUID taskId = getTaskIdFromAddress(httpExchange.getRequestURI().getPath(), true);

                    // Czy uźytkownik o podanym loginie istnieje?
                    final UserEntity user = findAndValidateUser(userData);

                    // Jeżeli wywołano GET z knkretnym identyfikatorem, to znajdź takie zadanie
                    TaskEntity task = null;
                    if (null != taskId) {
                        task = service.findTask(taskId);

                        if (task == null) {
                            throw new ToDoException(HttpStatus.NOT_FOUND, "Nie znaleziono zadania o ID = " + taskId.toString());
                        }
                    }

                    if (null != task) {
                        // odczyt knkretnego zadania

                        // użytkownik musi być autorem zadania
                        task.validateUser(user.getUsername());

                        // zwróć oczekiwane zadanie
                        response = task.toJson().getBytes(StandardCharsets.UTF_8);
                    } else {
                        // odczyt wszystkich zadań użytkownika

                        final List<TaskEntity> taskList = service.findTasksOfUser(user.getUsername());

                        StringBuilder responseTextBuilder = new StringBuilder("[");
                        for (TaskEntity userTask : taskList) {
                            responseTextBuilder.append(userTask.toJson()).append(",");
                        }
                        String responseText = responseTextBuilder.toString();

                        // usunięcie ostatniego przecinka
                        if (taskList.size() > 0) {
                            responseText = responseText.substring(0, responseText.length() - 1);
                        }

                        response = (responseText + "]").getBytes(StandardCharsets.UTF_8);
                    }

                    status = HttpStatus.OK;
                }
                break;

                case "PUT": {

                    LOGGER.info("Task Handler [PUT]");

                    // Czy przekazano ID zadania i czy jest ono dobre?
                    final UUID taskId = getTaskIdFromAddress(httpExchange.getRequestURI().getPath(), false);

                    // Czy request body istnieje i zawiera poprawne dane?
                    final Map<String, String> taskData = ToDoHandlersUtils.readRequestBody(httpExchange);
                    TaskEntity taskToUpdate = new TaskEntity();
                    taskToUpdate.setDescription(taskData.get("description"));
                    taskToUpdate.setDue(taskData.get("due"));

                    // Czy uźytkownik o podanym loginie istnieje?
                    final UserEntity user = findAndValidateUser(userData);

                    // Zadanie o podanym ID musi istnieć
                    TaskEntity task =  service.findTask(taskId);
                    if (null == task) {
                        throw new ToDoException(HttpStatus.NOT_FOUND, "Nie znaleziono zadania o ID = " + taskId.toString());
                    }

                    // użytkownik musi być autorem zadania
                    task.validateUser(user.getUsername());

                    // następuje aktualizacja
                    task = service.updateTask(taskId, taskToUpdate);
                    taskToUpdate.setId(task.getId());
                    response = taskToUpdate.toJson().getBytes();
                }
                break;

                case "DELETE": {

                    LOGGER.info("Task Handler [DELETE]");

                    // Czy przekazano ID zadania i czy jest ono dobre?
                    final UUID taskId = getTaskIdFromAddress(httpExchange.getRequestURI().getPath(), false);

                    // Czy uźytkownik o podanym loginie istnieje?
                    final UserEntity user = findAndValidateUser(userData);

                    // Zadanie o podanym ID musi istnieć
                    TaskEntity task =  service.findTask(taskId);
                    if (null == task) {
                        throw new ToDoException(HttpStatus.NOT_FOUND, "Nie znaleziono zadania o ID = " + taskId.toString());
                    }

                    // użytkownik musi być autorem zadania
                    task.validateUser(user.getUsername());

                    // następuje usunięcie
                    service.removeTask(taskId);
                    status = HttpStatus.OK;
                }
                break;

                default: {
                    throw new ToDoException(HttpStatus.METHOD_NOT_ALLOWED, "Unrecognized HTTP method for Task Handler.");
                }
            }
        } catch (ToDoException e) {
            status = e.getHttpStatus();
            LOGGER.info("Task Handler, exception: " + e.getMessage());
        }

        LOGGER.info("Task Handler, response code: " + status.debugStatus());

        httpExchange.sendResponseHeaders(status.getCode(), response.length);

        final OutputStream responseBody = httpExchange.getResponseBody();
        responseBody.write(response);
        LOGGER.info("Task Handler, response: `" + new String(response) + "`");

        httpExchange.close();

        System.out.println("-".repeat(64));
    }
}

