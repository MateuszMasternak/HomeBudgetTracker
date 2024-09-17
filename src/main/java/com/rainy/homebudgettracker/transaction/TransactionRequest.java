package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TransactionRequest {
    @NotNull(message = "Amount is required")
    BigDecimal amount;
    @NotNull(message = "Category is required")
    CategoryRequest category;
    @NotNull(message = "Date is required")
    LocalDate date;
    @NotNull(message = "Currency code is required")
    CurrencyCode currencyCode;
    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod;
}
