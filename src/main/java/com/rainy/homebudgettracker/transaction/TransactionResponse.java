package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.category.CategoryResponse;
import lombok.*;

import java.util.UUID;

@Builder
public record TransactionResponse(
    UUID id,
    String amount,
    CategoryResponse category,
    String date,
    AccountResponse account,
    String transactionMethod,
    String imageUrl,
    String details
) {}