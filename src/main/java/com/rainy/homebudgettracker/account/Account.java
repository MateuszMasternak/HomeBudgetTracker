package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.transaction.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"currencyCode", "user_id"})
})
public class Account {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private CurrencyCode currencyCode;
    @ColumnDefault("0")
    private BigDecimal balance;
    @ManyToOne
    private User user;
}
