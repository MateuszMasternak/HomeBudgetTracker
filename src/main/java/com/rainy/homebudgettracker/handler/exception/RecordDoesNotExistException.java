package com.rainy.homebudgettracker.handler.exception;

public class RecordDoesNotExistException extends Exception {
    public RecordDoesNotExistException(String message) {
        super(message);
    }
}
