package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.TransactionMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class TransactionRequest {
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    @NotNull(message = "Category name is required")
    private CategoryRequest categoryName;
    @NotNull(message = "Date is required")
    private LocalDate date;
    private CurrencyCode currencyCode;
    private BigDecimal exchangeRate;
    @NotNull(message = "Transaction method is required")
    private TransactionMethod transactionMethod;
    private String details;
}
