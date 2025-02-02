package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;

import java.util.UUID;

public class TestData {
    public static final String USER_SUB_1 = "550e8400-e29b-41d4-a716-446655440000";
    public static final String USER_SUB_2 = "550e8400-e29b-41d4-a716-446655440001";

    public static final Account ACCOUNT_1 = new Account(
            UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"),
            "USD account",
            CurrencyCode.USD,
            USER_SUB_1);

    public static final Account ACCOUNT_2 = new Account(
            UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773"),
            "EUR account",
            CurrencyCode.EUR,
            USER_SUB_1);

    public static final Account ACCOUNT_UPDATED = new Account(
            ACCOUNT_1.getId(),
            "Changed name",
            CurrencyCode.USD,
            USER_SUB_1);

    public static final Account ACCOUNT_OTHER_USER = new Account(
            UUID.fromString("312a1af8-a338-49ea-b67c-860062a10111"),
            "USD account",
            CurrencyCode.USD,
            USER_SUB_2);

    public static final AccountRequest ACCOUNT_REQUEST_1 = new AccountRequest("USD account", CurrencyCode.USD);
    public static final AccountRequest ACCOUNT_REQUEST_2 = new AccountRequest("EUR account", CurrencyCode.EUR);

    public static final AccountResponse ACCOUNT_RESPONSE_1 = new AccountResponse(
            ACCOUNT_1.getId(),
            ACCOUNT_1.getName(),
            ACCOUNT_1.getCurrencyCode().toString(),
            "0.00");

    public static final AccountResponse ACCOUNT_RESPONSE_2 = new AccountResponse(
            ACCOUNT_2.getId(),
            ACCOUNT_2.getName(),
            ACCOUNT_2.getCurrencyCode().toString(),
            "0.00");

    public static final AccountResponse ACCOUNT_RESPONSE_UPDATED = new AccountResponse(
            ACCOUNT_UPDATED.getId(),
            ACCOUNT_UPDATED.getName(),
            ACCOUNT_UPDATED.getCurrencyCode().toString(),
            "0.00");
}
