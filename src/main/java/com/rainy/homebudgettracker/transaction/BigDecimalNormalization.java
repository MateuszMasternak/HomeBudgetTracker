package com.rainy.homebudgettracker.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalNormalization {
    public static BigDecimal changeToZeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static BigDecimal normalize(BigDecimal value, int scale) {
        return changeToZeroIfNull(value).setScale(scale, RoundingMode.HALF_UP);
    }
}
