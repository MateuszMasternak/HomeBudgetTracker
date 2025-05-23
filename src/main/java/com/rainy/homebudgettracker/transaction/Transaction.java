package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.transaction.enums.TransactionMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private BigDecimal amount;
    @ManyToOne
    private Category category;
    private LocalDate date;
    private String userSub;
    @ManyToOne
    private Account account;
    private TransactionMethod transactionMethod;
    private String imageFilePath;
    private String details;
}
