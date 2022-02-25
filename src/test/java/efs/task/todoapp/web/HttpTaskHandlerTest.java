package efs.task.todoapp.web;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.utils.ToDoServerExtension;
import efs.task.todoapp.utils.ToDoTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.net.http.HttpResponse.BodyHandlers;
import static java.net.http.HttpRequest.BodyPublishers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@ExtendWith(ToDoServerExtension.class)
class HttpTaskHandlerTest {

    public static final String TODO_TASK_PATH = "http://localhost:8080/todo/task/";

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
    }

    public String testingTaskHandler(String jsonRequest, String method, String taskId, String authHeader, int expectedCode) throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_TASK_PATH + taskId))
                .header("auth", authHeader)
                .method(method, BodyPublishers.ofString(jsonRequest))
                .build();

        //when
        var httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());

        //then
        assertThat(httpResponse.statusCode()).as("Response status code").isEqualTo(expectedCode);

        return httpResponse.body();
    }

    private String[] createValidUserAdnReturnCredentials() {
        final String username = "janKowalski";
        final String password = "1234";
        final String validAuth = ToDoTestUtils.userDataToAuth(username, password);
        ToDoTestUtils.addUser(username, password);
        return new String[] {username, validAuth};
    }

    @Test
    @Timeout(1)
    void shouldReturnBadRequestStatusWhenWrongData() throws IOException, InterruptedException {
        System.out.println("TASK HANDLER TEST: \n\t BAD REQUEST CODE");

        final List<String[]> testParams = new ArrayList<>();
        final String[] userData = createValidUserAdnReturnCredentials();

        final String validTaskRequest = ToDoTestUtils.generateBodyFromTaskData("Kup mleko", "2021-06-09");
        final String invalidTaskRequestA = ToDoTestUtils.generateBodyFromTaskData("", "");
        final String invalidTaskRequestB = ToDoTestUtils.generateBodyFromTaskData("", "2021-06-09");
        final String invalidTaskRequestC = ToDoTestUtils.generateBodyFromTaskData("Kup chleb", "Dzisiaj");

        testParams.add(new String[] {"", ""});
        testParams.add(new String[] {"", userData[1]});
        testParams.add(new String[] {validTaskRequest, "@:@"});
        testParams.add(new String[] {invalidTaskRequestB, userData[1]});
        testParams.add(new String[] {invalidTaskRequestA, userData[1]});
        testParams.add(new String[] {invalidTaskRequestC, userData[1]});

        for (String[] params  : testParams) {
            testingTaskHandler(params[0], "POST", "", params[1], HttpStatus.BAD_REQUEST.getCode());
        }
    }

    @Test
    @Timeout(1)
    void shouldReturnCreatedStatusWhenGoodTask() throws IOException, InterruptedException {
        System.out.println("TASK HANDLER TEST: \n\t CREATED CODE");

        final String[] userData = createValidUserAdnReturnCredentials();

        final String validTaskRequest = ToDoTestUtils.generateBodyFromTaskData("Kup mleko", "2021-06-09");

        final String body = testingTaskHandler(validTaskRequest, "POST", "", userData[1], HttpStatus.CREATED.getCode());

        final Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        final Map<String, String> response = new Gson().fromJson(body, mapType);

        final String taskId = response.get("id");
        assertThat(taskId).isNotNull();
        assertThatNoException().isThrownBy(
                () -> UUID.fromString(taskId)
        );
;    }

    @Test
    @Timeout(1)
    void shouldReturnOkStatusWhenGettingTask() throws IOException, InterruptedException {
        System.out.println("TASK HANDLER TEST: \n\t CREATED CODE");

        final String[] userData = createValidUserAdnReturnCredentials();

        final TaskEntity task = ToDoTestUtils.addTask("aaaaa", "2022-01-01", userData[0]);
        final String taskId = task.getId().toString();

        final String body = testingTaskHandler("", "GET", taskId, userData[1], HttpStatus.OK.getCode());

        final TaskEntity returnedTask = new Gson().fromJson(body, TaskEntity.class);
        assertThat(task).isEqualTo(returnedTask);
    }

}
