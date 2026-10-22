package com.example.notificationservice.exception;

// Thrown when a message was readable JSON but has no usable content. Retrying cannot fix it.
public class InvalidEventException extends RuntimeException {

    public InvalidEventException(String message) {
        super(message);
    }
}
