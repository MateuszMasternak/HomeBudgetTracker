package com.rainy.homebudgettracker.handler.exception;

public class CategoryAssociatedWithTransactionException extends RuntimeException {
    public CategoryAssociatedWithTransactionException(String message) {
        super(message);
    }

    public CategoryAssociatedWithTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
