package ru.lcp.kernel;

import lombok.Data;

import java.util.Date;

@Data
public class Response {
    private int status;
    private String message;
    private Date timestamp;

    public Response(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = new Date();
    }
}
