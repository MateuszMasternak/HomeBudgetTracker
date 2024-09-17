package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.category.CategoryResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransactionResponse {
    private Long id;
    private String amount;
    private CategoryResponse category;
    private String date;
    private AccountResponse account;
    private String paymentMethod;
}
