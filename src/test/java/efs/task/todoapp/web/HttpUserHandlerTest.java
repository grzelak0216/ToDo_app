package efs.task.todoapp.web;

import efs.task.todoapp.utils.ToDoServerExtension;
import efs.task.todoapp.utils.ToDoTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static java.net.http.HttpResponse.BodyHandlers;
import static java.net.http.HttpRequest.BodyPublishers;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ToDoServerExtension.class)
class HttpUserHandlerTest {

    public static final String TODO_USER_PATH = "http://localhost:8080/todo/user/";

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
    }

    void testingUserHandler(String jsonRequest, int expectedCode) throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_USER_PATH))
                .POST(BodyPublishers.ofString(jsonRequest))
                .build();

        //when
        var httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());

        //then
        assertThat(httpResponse.statusCode()).as("Response status code").isEqualTo(expectedCode);
    }

    @Test
    @Timeout(1)
    void shouldReturnCreatedCodeUserAdded() throws IOException, InterruptedException {
        final String jsonRequest = ToDoTestUtils.generateBodyFromUserData("admin", "admin");
        System.out.println("USER HANDLER TEST: \n\t CREATED CODE, jsonRequest: `" + jsonRequest + "`");
        testingUserHandler(jsonRequest, HttpStatus.CREATED.getCode());
    }

    @Test
    @Timeout(1)
    void shouldReturnBadRequestEmptyBody() throws IOException, InterruptedException {
        final String jsonRequest = ToDoTestUtils.generateBodyFromUserData("", "");
        System.out.println("USER HANDLER TEST: \n\t BAD_REQUEST CODE, jsonRequest: `" + jsonRequest + "`");
        testingUserHandler(jsonRequest, HttpStatus.BAD_REQUEST.getCode());
    }

    @Test
    @Timeout(1)
    void shouldReturnConflictAddingSameUser() throws IOException, InterruptedException {
        final String username = "user12345";
        final String password = "pass54321";
        ToDoTestUtils.addUser(username, password);
        final String jsonRequest = ToDoTestUtils.generateBodyFromUserData(username, password);
        System.out.println("USER HANDLER TEST: \n\t CONFLICT CODE, jsonRequest: `" + jsonRequest + "`");
        testingUserHandler(jsonRequest, HttpStatus.CONFLICT.getCode());
    }

}
