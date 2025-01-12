package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;

import java.util.List;
import java.util.UUID;

public class TestData {
    public static final List<String> userSubs = List.of(
            "550e8400-e29b-41d4-a716-446655440000",
            "550e8400-e29b-41d4-a716-446655440001"
    );

    public static final List<Account> accounts = List.of(
            new Account(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"),
                    "USD account",
                    CurrencyCode.USD,
                    userSubs.get(0)),
            new Account(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773"),
                    "EUR account",
                    CurrencyCode.EUR,
                    userSubs.get(0)),
            new Account(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"),
                    "Changed name",
                    CurrencyCode.USD,
                    userSubs.get(0)),
            new Account(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10111"),
                    "USD account",
                    CurrencyCode.USD,
                    userSubs.get(1))

    );

    public static final List<AccountRequest> accountRequests = List.of(
            new AccountRequest("USD account", CurrencyCode.USD),
            new AccountRequest("EUR account", CurrencyCode.EUR)
    );

    public static final List<AccountResponse> accountResponses = List.of(
            new AccountResponse(accounts.get(0).getId(),
                    accounts.get(0).getName(),
                    accounts.get(0).getCurrencyCode().toString(),
                    "0.00"),
            new AccountResponse(accounts.get(1).getId(),
                    accounts.get(1).getName(),
                    accounts.get(1).getCurrencyCode().toString(),
                    "0.00"),
            new AccountResponse(accounts.get(2).getId(),
                    accounts.get(2).getName(),
                    accounts.get(2).getCurrencyCode().toString(),
                    "0.00")
    );
}
