package ca.gbc.eventservice.service;

public class MissingEventException extends RuntimeException {
    public MissingEventException(String message) {
        super(message);
    }
}

