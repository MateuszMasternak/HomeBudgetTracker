package com.rainy.homebudgettracker.account;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class AccountResponse {
    private Long id;
    private String name;
    private String currencyCode;
}
