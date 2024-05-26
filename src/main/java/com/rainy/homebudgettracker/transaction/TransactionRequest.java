package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TransactionRequest {
    @NotEmpty(message = "Amount is required")
    @NotBlank(message = "Amount is required")
    BigDecimal amount;
    @NotEmpty(message = "Category is required")
    @NotBlank(message = "Category is required")
    CategoryRequest category;
    @NotEmpty(message = "Date is required")
    @NotBlank(message = "Date is required")
    LocalDate date;
}
