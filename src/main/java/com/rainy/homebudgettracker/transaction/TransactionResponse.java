package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.category.CategoryResponse;
import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class TransactionResponse {
    private Long id;
    private String amount;
    private CategoryResponse category;
    private String date;
    private AccountResponse account;
    private String paymentMethod;
    private String imageUrl;
    private String details;
}
