package com.rainy.homebudgettracker.user;

import lombok.*;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DefaultCurrencyResponseRequest {
    private String currencyCode;
}
