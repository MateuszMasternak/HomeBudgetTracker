package com.rainy.homebudgettracker.handler.exception;

public class ExpiredConfirmationTokenException extends Exception {
    public ExpiredConfirmationTokenException(String message) {
        super(message);
    }
}
