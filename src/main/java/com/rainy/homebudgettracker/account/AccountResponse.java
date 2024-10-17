package com.rainy.homebudgettracker.account;

import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class AccountResponse {
    private Long id;
    private String name;
    private String currencyCode;
}
