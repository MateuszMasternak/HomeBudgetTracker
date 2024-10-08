package com.rainy.homebudgettracker.handler.exception;

public class EmailAlreadyInUseException extends Exception {
    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}
