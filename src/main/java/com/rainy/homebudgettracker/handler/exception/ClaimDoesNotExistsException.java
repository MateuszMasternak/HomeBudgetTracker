package com.rainy.homebudgettracker.handler.exception;

public class ClaimDoesNotExistsException extends RuntimeException {
    public ClaimDoesNotExistsException(String message) {
        super(message);
    }

    public ClaimDoesNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
