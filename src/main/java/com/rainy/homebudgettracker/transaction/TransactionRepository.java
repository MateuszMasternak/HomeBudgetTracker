package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Iterable<Transaction> findAllByUser(User user);
    Iterable<Transaction> findAllByUserAndCurrencyCode(User user, CurrencyCode currencyCode);
    Page<Transaction> findAllByUser(User user, Pageable pageable);
    Page<Transaction> findAllByUserAndCurrencyCode(User user, CurrencyCode currencyCode, Pageable pageable);
    Page<Transaction> findAllByUserAndCategory(User user, Category category, Pageable pageable);
    Page<Transaction> findAllByUserAndCurrencyCodeAndCategory(
            User user, CurrencyCode currencyCode, Category category, Pageable pageable
    );
    Page<Transaction> findAllByUserAndDateBetween(
            User user, LocalDate startDate, LocalDate endDate, Pageable pageable
    );
    Page<Transaction> findAllByUserAndCurrencyCodeAndDateBetween(
            User user, CurrencyCode currencyCode, LocalDate startDate, LocalDate endDate, Pageable pageable
    );
    Page<Transaction> findAllByUserAndCategoryAndDateBetween(
            User user, Category category, LocalDate startDate, LocalDate endDate, Pageable pageable
    );
    Page<Transaction> findAllByUserAndCurrencyCodeAndCategoryAndDateBetween(
            User user, CurrencyCode currencyCode, Category category, LocalDate startDate, LocalDate endDate,
            Pageable pageable
    );
    boolean existsByCategory(Category category);
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user AND t.amount > 0 AND t.currencyCode = :currencyCode")
    BigDecimal sumPositiveAmountByUser(User user, CurrencyCode currencyCode);
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user AND t.amount < 0 AND t.currencyCode = :currencyCode")
    BigDecimal sumNegativeAmountByUser(User user, CurrencyCode currencyCode);
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user AND t.currencyCode = :currencyCode")
    BigDecimal sumAmountByUser(User user, CurrencyCode currencyCode);
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user AND t.date BETWEEN :startDate AND :endDate AND t.currencyCode = :currencyCode")
    BigDecimal sumAmountByUserAndDateBetween(User user, CurrencyCode currencyCode, LocalDate startDate,
                                             LocalDate endDate);
}
