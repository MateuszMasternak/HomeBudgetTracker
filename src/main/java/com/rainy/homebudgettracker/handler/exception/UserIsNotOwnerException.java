package com.rainy.homebudgettracker.handler.exception;

public class UserIsNotOwnerException extends RuntimeException {
    public UserIsNotOwnerException(String message) {
        super(message);
    }

    public UserIsNotOwnerException(String message, Throwable cause) {
        super(message, cause);
    }
}
