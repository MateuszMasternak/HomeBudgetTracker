package com.rainy.homebudgettracker.mapper;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.dto.TransactionRequest;
import com.rainy.homebudgettracker.transaction.dto.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.TransactionMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class ModelMapperTest {
    @InjectMocks
    ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void shouldMapCategoryToCategoryResponse() {
        var category = Category.builder()
                .id(TestData.CATEGORY_ID)
                .name(TestData.CATEGORY_NAME)
                .userSub(TestData.USER_SUB)
                .build();

        var returnedCategoryResponse = modelMapper.map(category, CategoryResponse.class);

        var categoryResponse = CategoryResponse.builder()
                .id(TestData.CATEGORY_ID)
                .name(TestData.CATEGORY_NAME)
                .build();

        assertEquals(categoryResponse, returnedCategoryResponse);
    }

    @Test
    public void shouldMapAccountToAccountResponse() {
        var account = Account.builder()
                .id(TestData.ACCOUNT_ID)
                .name(TestData.ACCOUNT_NAME)
                .currencyCode(TestData.ACCOUNT_CURRENCY)
                .userSub(TestData.USER_SUB)
                .build();

        var returnedAccountResponse = modelMapper.map(account, AccountResponse.class);

        var accountResponse = AccountResponse.builder()
                .id(TestData.ACCOUNT_ID)
                .name(TestData.ACCOUNT_NAME)
                .currencyCode(TestData.ACCOUNT_CURRENCY.name())
                .build();

        assertEquals(accountResponse, returnedAccountResponse);
    }

    @Test
    public void shouldMapAccountToAccountResponseWithBalance() {
        var account = Account.builder()
                .id(TestData.ACCOUNT_ID)
                .name(TestData.ACCOUNT_NAME)
                .currencyCode(TestData.ACCOUNT_CURRENCY)
                .userSub(TestData.USER_SUB)
                .build();

        var returnedAccountResponse = modelMapper.map(account, AccountResponse.class, TestData.AMOUNT);

        var accountResponse = AccountResponse.builder()
                .id(TestData.ACCOUNT_ID)
                .name(TestData.ACCOUNT_NAME)
                .currencyCode(TestData.ACCOUNT_CURRENCY.name())
                .balance("100.00")
                .build();

        assertEquals(accountResponse, returnedAccountResponse);
    }

    @Test
    public void shouldMapTransactionRequestToTransaction1() {
        var account = Account.builder()
                .id(TestData.ACCOUNT_ID)
                .name(TestData.ACCOUNT_NAME)
                .currencyCode(TestData.ACCOUNT_CURRENCY)
                .userSub(TestData.USER_SUB)
                .build();
        var category = Category.builder()
                .id(TestData.CATEGORY_ID)
                .name(TestData.CATEGORY_NAME)
                .userSub(TestData.USER_SUB)
                .build();

        var transactionRequest = TransactionRequest.builder()
                .amount(TestData.AMOUNT)
                .date(TestData.TRANSACTION_DATE)
                .transactionMethod(TransactionMethod.CASH)
                .categoryName(CategoryRequest.builder().name(TestData.CATEGORY_NAME).build())
                .currencyCode(TestData.ACCOUNT_CURRENCY)
                .details(TestData.DETAILS)
                .build();

        var returnedTransaction = modelMapper.map(transactionRequest, Transaction.class, TestData.USER_SUB, category, account);

        var transaction = Transaction.builder()
                .amount(transactionRequest.getAmount().setScale(2, RoundingMode.HALF_UP))
                .category(category)
                .date(transactionRequest.getDate())
                .account(account)
                .transactionMethod(transactionRequest.getTransactionMethod())
                .userSub(TestData.USER_SUB)
                .details(transactionRequest.getDetails())
                .build();

        assertEquals(transaction, returnedTransaction);
    }

    @Test
    public void shouldMapTransactionToTransactionResponse() {
        var account = Account.builder()
                .id(TestData.ACCOUNT_ID)
                .name(TestData.ACCOUNT_NAME)
                .currencyCode(TestData.ACCOUNT_CURRENCY)
                .userSub(TestData.USER_SUB)
                .build();
        var category = Category.builder()
                .id(TestData.CATEGORY_ID)
                .name(TestData.CATEGORY_NAME)
                .userSub(TestData.USER_SUB)
                .build();

        var transaction = Transaction.builder()
                .id(TestData.ACCOUNT_ID)
                .amount(TestData.AMOUNT)
                .date(TestData.TRANSACTION_DATE)
                .transactionMethod(TransactionMethod.CASH)
                .category(category)
                .account(account)
                .userSub(TestData.USER_SUB)
                .details(TestData.DETAILS)
                .build();

        var returnedTransactionResponse = modelMapper.map(transaction, TransactionResponse.class, TestData.IMAGE_URL);

        var transactionResponse = TransactionResponse.builder()
                .id(transaction.getId())
                .amount("100.00")
                .category(CategoryResponse.builder()
                        .id(TestData.CATEGORY_ID)
                        .name(TestData.CATEGORY_NAME)
                        .build())
                .date(String.valueOf(transaction.getDate()))
                .account(AccountResponse.builder()
                        .id(TestData.ACCOUNT_ID)
                        .name(TestData.ACCOUNT_NAME)
                        .currencyCode(TestData.ACCOUNT_CURRENCY.name())
                        .build())
                .transactionMethod(transaction.getTransactionMethod().name())
                .details(TestData.DETAILS)
                .hasImage(true)
                .build();

        assertEquals(transactionResponse, returnedTransactionResponse);
    }
}