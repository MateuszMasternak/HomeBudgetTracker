package com.rainy.homebudgettracker.user;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class DefaultCurrency {
    @Id
    private String userSub;
    private CurrencyCode currencyCode;
}
