package com.rainy.homebudgettracker.account;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccountResponse {
    private Long id;
    private String name;
    private String currencyCode;
}
