package com.rainy.homebudgettracker.exchange;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyConverter {
    public static BigDecimal convert(BigDecimal amount, BigDecimal exchangeRate, int scale) {
        return amount.multiply(exchangeRate).setScale(scale, RoundingMode.HALF_UP);
    }
}
