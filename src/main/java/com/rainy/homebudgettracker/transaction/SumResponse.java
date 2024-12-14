package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.category.CategoryResponse;
import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class SumResponse {
    private String amount;
    private AccountResponse account;
    private CategoryResponse category;
}
