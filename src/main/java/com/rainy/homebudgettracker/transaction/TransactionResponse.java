package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.category.CategoryResponse;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class TransactionResponse {
    private UUID id;
    private String amount;
    private CategoryResponse category;
    private String date;
    private AccountResponse account;
    private String transactionMethod;
    private String imageUrl;
    private String details;
}
