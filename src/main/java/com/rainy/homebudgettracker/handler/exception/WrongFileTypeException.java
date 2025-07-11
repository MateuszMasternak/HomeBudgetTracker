package com.rainy.homebudgettracker.handler.exception;

public class WrongFileTypeException extends RuntimeException {
    public WrongFileTypeException(String message) {
        super(message);
    }

    public WrongFileTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
