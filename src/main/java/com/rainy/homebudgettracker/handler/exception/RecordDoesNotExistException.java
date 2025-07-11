package com.rainy.homebudgettracker.handler.exception;

public class RecordDoesNotExistException extends RuntimeException {
    public RecordDoesNotExistException(String message) {
        super(message);
    }

    public RecordDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
