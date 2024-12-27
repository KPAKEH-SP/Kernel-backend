package ru.lcp.kernel.exceptions;

public class ChatNotFound extends RuntimeException {
    public ChatNotFound() {
        super("Chat not found");
    }
}
