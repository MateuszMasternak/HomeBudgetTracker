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
    Optional<Account> findByUserAndCurrencyCode(User user, CurrencyCode currencyCode);
    @Modifying
    @Query("update Account a set a.name = :name where a.user = :user and a.currencyCode = :currencyCode")
    void updateAccountName(User user, String name, CurrencyCode currencyCode);
    boolean existsByUserAndCurrencyCode(User user, CurrencyCode currencyCode);
    void deleteAllByUser(User user);
}
