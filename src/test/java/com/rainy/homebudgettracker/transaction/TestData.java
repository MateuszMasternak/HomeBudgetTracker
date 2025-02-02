package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.TransactionMethod;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public class TestData {

    public static final String USER_SUB = "550e8400-e29b-41d4-a716-446655440000";
    public static final String USER_SUB_2 = "550e8400-e29b-41d4-a716-446655440001";

    public static final String IMAGE_URL = "https://example.com/image.jpg";

    public static final String ACCOUNT_ID = "212a0e7e-24c3-4774-a46b-741d89072fad";
    public static final String ACCOUNT_ID_2 = "43823673-fa1b-45fd-900f-374505b9a454";
    public static final String OTHER_USER_ACCOUNT_ID = "77f57e8c-f7a4-4ff3-bb18-bd448b7a3019";
    public static final String NON_EXISTENT_ACCOUNT_ID = "4f23541e-b244-4e18-a17e-620e5d6feb1a";

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

    public static final Pageable PAGEABLE = PageRequest.of(0, 10, Sort.by("date").descending());
    public static final Pageable PAGEABLE_2 = PageRequest.of(1, 10, Sort.by("date").descending());

    public static final String TRANSACTION_ID = "212a0e7e-24c3-4774-a46b-741d89072fad";
    public static final String TRANSACTION_ID_2 = "77f57e8c-f7a4-4ff3-bb18-bd448b7a3019";
    public static final String TRANSACTION_ID_3 = "4f23541e-b244-4e18-a17e-620e5d6feb1a";

    public static final Transaction TRANSACTION = Transaction.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .userSub(USER_SUB)
            .amount(BigDecimal.valueOf(100, 2))
            .date(LocalDate.of(2024, 1, 1))
            .transactionMethod(TransactionMethod.CASH)
            .category(CATEGORY)
            .account(ACCOUNT)
            .build();
    public static final Transaction TRANSACTION_2 = Transaction.builder()
            .id(UUID.fromString(TRANSACTION_ID_2))
            .userSub(USER_SUB_2)
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
            .build();
    public static final Transaction CONVERTED_TRANSACTION_2 = Transaction.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
            .date(LocalDate.of(2024, 1, 1))
            .transactionMethod(TransactionMethod.CASH)
            .category(CATEGORY)
            .account(ACCOUNT_2)
            .build();
    public static final Transaction CONVERTED_TRANSACTION_3 = Transaction.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .amount(BigDecimal.valueOf(422).setScale(2, RoundingMode.HALF_UP))
            .date(LocalDate.of(2024, 1, 1))
            .transactionMethod(TransactionMethod.CASH)
            .category(CATEGORY)
            .account(ACCOUNT_2)
            .details("EUR->PLN: 4.22")
            .build();

    public static final TransactionResponse TRANSACTION_RESPONSE = TransactionResponse.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .amount("100.00")
            .date("2024-01-01")
            .transactionMethod("CASH")
            .category(CATEGORY_RESPONSE)
            .account(ACCOUNT_RESPONSE)
            .build();
    public static final TransactionResponse CONVERTED_TRANSACTION_RESPONSE = TransactionResponse.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .amount("421.00")
            .date("2024-01-01")
            .transactionMethod("CASH")
            .category(CATEGORY_RESPONSE)
            .account(ACCOUNT_RESPONSE_2)
            .build();
    public static final TransactionResponse CONVERTED_TRANSACTION_RESPONSE_2 = TransactionResponse.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .amount("421.00")
            .date("2024-01-01")
            .transactionMethod("CASH")
            .category(CATEGORY_RESPONSE)
            .account(ACCOUNT_RESPONSE_2)
            .build();
    public static final TransactionResponse CONVERTED_TRANSACTION_RESPONSE_3 = TransactionResponse.builder()
            .id(UUID.fromString(TRANSACTION_ID))
            .amount("422.00")
            .date("2024-01-01")
            .transactionMethod("CASH")
            .category(CATEGORY_RESPONSE)
            .account(ACCOUNT_RESPONSE_2)
            .details("EUR->PLN: 4.22")
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
            .build();
    public static final TransactionRequest CONVERTED_TRANSACTION_REQUEST_2 = TransactionRequest.builder()
            .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
            .categoryName(CATEGORY_REQUEST)
            .date(LocalDate.of(2024, 1, 1))
            .currencyCode(CurrencyCode.PLN)
            .transactionMethod(TransactionMethod.CASH)
            .build();
    public static final TransactionRequest CONVERTED_TRANSACTION_REQUEST_3 = TransactionRequest.builder()
            .amount(BigDecimal.valueOf(422).setScale(2, RoundingMode.HALF_UP))
            .categoryName(CATEGORY_REQUEST)
            .date(LocalDate.of(2024, 1, 1))
            .currencyCode(CurrencyCode.PLN)
            .transactionMethod(TransactionMethod.CASH)
            .details("EUR->PLN: 4.22")
            .build();

    public static final SumResponse SUM_RESPONSE = SumResponse.builder().amount("100.10").build();
    public static final SumResponse SUM_RESPONSE_2 = SumResponse.builder().amount("0.00").build();
}
