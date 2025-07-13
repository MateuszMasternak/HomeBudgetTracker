package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currencyCode;
    @Column(nullable = false)
    private String userSub;
}
