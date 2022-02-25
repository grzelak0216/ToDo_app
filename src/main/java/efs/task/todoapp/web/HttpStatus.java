package efs.task.todoapp.web;

public enum HttpStatus {
    OK (200, "OK"),
    CREATED (201, "Created"),
    BAD_REQUEST (400, "Bad Request"),
    UNAUTHORIZED (401, "Unauthorized"),
    FORBIDDEN (403, "Forbidden"),
    NOT_FOUND (404, "Not Found"),
    METHOD_NOT_ALLOWED (405, "Method Nod Allowed"),
    CONFLICT (409, "Conflict");

    private final int code;
    private final String title;

    HttpStatus(int code, String title) {
        this.code = code;
        this.title = title;
    }

    public int getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String debugStatus() {
        return "[" + code + "] (" + title + ")";
    }

    public String htmlBody() {
        return "<h1>" + code + ": " + title + "</h1>";
    }
}
