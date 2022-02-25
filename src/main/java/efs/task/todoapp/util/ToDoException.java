package efs.task.todoapp.util;

import efs.task.todoapp.web.HttpStatus;

public class ToDoException extends RuntimeException {

    HttpStatus httpStatus;

    public ToDoException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
