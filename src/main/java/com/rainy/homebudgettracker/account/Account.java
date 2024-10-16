package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class Account {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private CurrencyCode currencyCode;
    @ManyToOne
    private User user;
}
