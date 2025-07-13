package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.transaction.enums.TransactionMethod;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class TransactionUpdateRequest {
    private String categoryName;
    private TransactionMethod transactionMethod;
    private String details;
}
