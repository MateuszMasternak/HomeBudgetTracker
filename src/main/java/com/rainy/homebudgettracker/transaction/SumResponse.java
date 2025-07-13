package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.category.CategoryResponse;
import lombok.*;

@Builder
public record SumResponse(
    String amount,
    AccountResponse account,
    CategoryResponse category
) {}
