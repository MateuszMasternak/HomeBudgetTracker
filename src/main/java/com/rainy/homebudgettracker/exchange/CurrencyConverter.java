package com.rainy.homebudgettracker.exchange;

import java.math.BigDecimal;

import static com.rainy.homebudgettracker.transaction.BigDecimalNormalization.normalize;

public class CurrencyConverter {
    public static BigDecimal convert(BigDecimal amount, BigDecimal exchangeRate, int scale) {
        return normalize(amount.multiply(exchangeRate), scale);
    }
}
