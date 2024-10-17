package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class TransactionRequest {
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    @NotNull(message = "Category name is required")
    private CategoryRequest categoryName;
    @NotNull(message = "Date is required")
    private LocalDate date;
    @NotNull(message = "Currency code is required")
    private CurrencyCode currencyCode;
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    private String details;
}
