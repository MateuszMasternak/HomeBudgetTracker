package com.rainy.homebudgettracker.handler.exception;

public class ExchangeRateApiException extends RuntimeException {
    public ExchangeRateApiException(String message) {
        super(message);
    }
}
