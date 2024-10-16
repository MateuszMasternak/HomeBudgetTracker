package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Iterable<Account> findAllByUser(User user);
    @Modifying
    @Query("update Account a set a.name = :name where a.id = :id")
    void updateAccountName(Long id, String name);
    void deleteAllByUser(User user);
}
