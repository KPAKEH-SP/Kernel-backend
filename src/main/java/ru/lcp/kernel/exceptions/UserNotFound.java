package ru.lcp.kernel.exceptions;

public class UserNotFound extends Exception {
    public UserNotFound() {
        super("User not found");
    }
}
