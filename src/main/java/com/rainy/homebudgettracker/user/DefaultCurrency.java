package com.rainy.homebudgettracker.user;

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
    private String currencyCode;
}
