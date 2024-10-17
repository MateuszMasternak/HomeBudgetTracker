package com.rainy.homebudgettracker.account;

import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class AccountResponse {
    private Long id;
    private String name;
    private String currencyCode;
}
