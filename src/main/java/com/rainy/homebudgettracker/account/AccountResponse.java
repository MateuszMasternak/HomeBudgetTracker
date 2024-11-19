package com.rainy.homebudgettracker.account;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class AccountResponse {
    private UUID id;
    private String name;
    private String currencyCode;
    private String balance;
}
