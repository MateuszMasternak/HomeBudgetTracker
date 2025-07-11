package com.rainy.homebudgettracker.handler.exception;

public class PremiumStatusRequiredException extends RuntimeException {
    public PremiumStatusRequiredException(String message) {
        super(message);
    }

    public PremiumStatusRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
