package com.rainy.homebudgettracker.mapper;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public class TestData {
    public static final String USER_SUB = "550e8400-e29b-41d4-a716-446655440000";
    public static final UUID CATEGORY_ID = UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903");
    public static final String CATEGORY_NAME = "Food";
    public static final UUID ACCOUNT_ID = UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903");
    public static final String ACCOUNT_NAME = "Main";
    public static final CurrencyCode ACCOUNT_CURRENCY = CurrencyCode.USD;
    public static final BigDecimal AMOUNT = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
    public static final String DETAILS = "Details";
    public static final String IMAGE_URL = "link-to-image";
    public static final LocalDate TRANSACTION_DATE = LocalDate.now();
}
