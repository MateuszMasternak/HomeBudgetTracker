package com.rainy.homebudgettracker.handler.exception;

public class QuotaReachedException extends RuntimeException {
    public QuotaReachedException(String message) {
        super(message);
    }
}
