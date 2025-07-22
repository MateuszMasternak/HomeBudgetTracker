package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.TransactionMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public class TestData {

    public static final String USER_SUB = "550e8400-e29b-41d4-a716-446655440000";
    public static final String USER_SUB_2 = "550e8400-e29b-41d4-a716-446655440001";

    public static final String ACCOUNT_ID = "212a0e7e-24c3-4774-a46b-741d89072fad";
    public static final String ACCOUNT_ID_2 = "43823673-fa1b-45fd-900f-374505b9a454";

    public static final LocalDate TEST_DATE = LocalDate.of(2025, 7, 15);

    public static final Account ACCOUNT = Account.builder()
            .id(UUID.fromString(ACCOUNT_ID))
            .name("USD account")
            .currencyCode(CurrencyCode.USD)
            .userSub(USER_SUB)
            .build();
    public static final Account ACCOUNT_2 = Account.builder()
            .id(UUID.fromString(ACCOUNT_ID_2))
            .name("PLN account")
            .currencyCode(CurrencyCode.PLN)
            .userSub(USER_SUB)
            .build();

    public static final AccountResponse ACCOUNT_RESPONSE = AccountResponse.builder()
            .id(UUID.fromString(ACCOUNT_ID))
            .name("USD account")
            .currencyCode("USD")
            .build();
    public static final AccountResponse ACCOUNT_RESPONSE_2 = AccountResponse.builder()
            .id(UUID.fromString(ACCOUNT_ID_2))
            .name("PLN account")
            .currencyCode("PLN")
            .build();

    public static final String CATEGORY_ID = "212a0e7e-24c3-4774-a46b-741d89072fad";

    public static final Category CATEGORY = Category.builder()
            .id(UUID.fromString(CATEGORY_ID))
            .name("Food")
            .userSub(USER_SUB)
            .build();

    public static final CategoryResponse CATEGORY_RESPONSE = CategoryResponse.builder()
            .id(UUID.fromString(CATEGORY_ID))
            .name("Food")
            .build();

    public static final CategoryRequest CATEGORY_REQUEST = CategoryRequest.builder()
            .name("Food")
            .build();

    public static final String TRANSACTION_ID = "212a0e7e-24c3-4774-a46b-741d89072fad";

    public static final Transaction TRANSACTION = Transaction.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .userSub(USER_SUB)
            .amount(BigDecimal.valueOf(100, 2))
            .date(LocalDate.of(2024, 1, 1))
            .transactionMethod(TransactionMethod.CASH)
            .category(CATEGORY)
            .account(ACCOUNT)
            .build();
    public static final Transaction CONVERTED_TRANSACTION = Transaction.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .userSub(USER_SUB)
            .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
            .date(LocalDate.of(2024, 1, 1))
            .transactionMethod(TransactionMethod.CASH)
            .category(CATEGORY)
            .account(ACCOUNT)
            .details("USD->PLN: 4.21")
            .build();
    public static final Transaction CONVERTED_TRANSACTION_2 = Transaction.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .amount(BigDecimal.valueOf(416).setScale(2, RoundingMode.HALF_UP))
            .date(TEST_DATE)
            .transactionMethod(TransactionMethod.CASH)
            .category(CATEGORY)
            .account(ACCOUNT_2)
            .details("EUR->PLN: 0.24 - " + TEST_DATE)
            .build();

    public static final TransactionResponse TRANSACTION_RESPONSE = TransactionResponse.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .amount("100.00")
            .date(TEST_DATE.toString())
            .transactionMethod("CASH")
            .category(CATEGORY_RESPONSE)
            .account(ACCOUNT_RESPONSE)
            .build();
    public static final TransactionResponse CONVERTED_TRANSACTION_RESPONSE_2 = TransactionResponse.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .amount("416.00")
            .date(TEST_DATE.toString())
            .transactionMethod("CASH")
            .category(CATEGORY_RESPONSE)
            .account(ACCOUNT_RESPONSE_2)
            .details("EUR->PLN: 0.24 - " + TEST_DATE)
            .build();

    public static final TransactionRequest TRANSACTION_REQUEST = TransactionRequest.builder()
            .amount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP))
            .categoryName(CATEGORY_REQUEST)
            .date(LocalDate.of(2024, 1, 1))
            .currencyCode(CurrencyCode.USD)
            .transactionMethod(TransactionMethod.CASH)
            .build();
    public static final TransactionRequest CONVERTED_TRANSACTION_REQUEST = TransactionRequest.builder()
            .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
            .categoryName(CATEGORY_REQUEST)
            .date(LocalDate.of(2024, 1, 1))
            .currencyCode(CurrencyCode.PLN)
            .transactionMethod(TransactionMethod.CASH)
            .details("PLN->EUR: 4.21")
            .build();
    public static final TransactionRequest CONVERTED_TRANSACTION_REQUEST_2 = TransactionRequest.builder()
            .amount(BigDecimal.valueOf(416).setScale(2, RoundingMode.HALF_UP))
            .categoryName(CATEGORY_REQUEST)
            .date(TestData.TEST_DATE)
            .currencyCode(CurrencyCode.EUR)
            .transactionMethod(TransactionMethod.CASH)
            .details("EUR->PLN: 0.24 - " + TEST_DATE)
            .build();
}
