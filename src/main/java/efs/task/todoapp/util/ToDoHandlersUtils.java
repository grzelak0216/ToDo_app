package efs.task.todoapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import efs.task.todoapp.ToDoApplication;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.web.HttpStatus;

public class ToDoHandlersUtils {

    private static final Logger LOGGER = Logger.getLogger(ToDoHandlersUtils.class.getName());

    public static Map<String, String> readRequestBody(HttpExchange httpExchange) throws IOException, ToDoException {
        // odczyt i sprawdzenie poprawnści request body (format Json)

        final InputStream requestBody = httpExchange.getRequestBody();
        final String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        requestBody.close();

        LOGGER.info("request: body `" + body + "`");

        try {
            if (null == body || body.isBlank()) {
                throw new Exception();
            }

            final Type mapType = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> jsonData = new Gson().fromJson(body, mapType);

            if (null == jsonData || jsonData.size() <= 0) {
                throw new Exception();
            }

            return jsonData;
        }
        catch (Exception e) {
            throw new ToDoException(HttpStatus.BAD_REQUEST, "Niepoprawne request body - nie jest w formacie Json.");
        }
    }

}
