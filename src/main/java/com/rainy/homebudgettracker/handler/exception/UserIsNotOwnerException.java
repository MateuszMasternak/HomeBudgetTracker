package com.rainy.homebudgettracker.handler.exception;

public class UserIsNotOwnerException extends Exception {
    public UserIsNotOwnerException(String message) {
        super(message);
    }
}
