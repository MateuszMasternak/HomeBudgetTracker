package com.rainy.homebudgettracker.transaction;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransactionResponse {
    private Long id;
    private String amount;
    private String category;
    private String date;
}
