package com.rainy.homebudgettracker.handler.exception;

public class RecordAlreadyExistsException extends Exception {
    public RecordAlreadyExistsException(String message) {
        super(message);
    }
}
