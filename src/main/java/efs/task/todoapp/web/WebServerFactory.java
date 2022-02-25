package efs.task.todoapp.web;

import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserRepository;
import efs.task.todoapp.service.ToDoService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServerFactory {

    public static ToDoService lastService;

    public static HttpServer createServer(){
        String hostName = "localhost";
        Integer portNumber = 8080;

        try {
            final HttpServer httpServer = HttpServer.create(new InetSocketAddress(hostName, portNumber), 0);

            ToDoService service = new ToDoService(new UserRepository(), new TaskRepository());
            httpServer.createContext("/todo/task", new HttpTaskHandler(service));
            httpServer.createContext("/todo/user", new HttpUserHandler(service));
            lastService = service;

            return httpServer;
        } catch (IOException e){
            throw new RuntimeException("Unable to start server", e);
        }
    }
}
