package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.transaction.enums.PaymentMethod;
import com.rainy.homebudgettracker.user.User;
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
    @ManyToOne
    private User user;
    @ManyToOne
    private Account account;
    private PaymentMethod paymentMethod;
    private String imageFilePath;
    private String details;
}
