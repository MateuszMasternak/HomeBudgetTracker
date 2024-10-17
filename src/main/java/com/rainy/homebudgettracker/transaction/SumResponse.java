package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.AccountResponse;
import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class SumResponse {
    private String amount;
    private AccountResponse account;
}
