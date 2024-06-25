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
    Page<Transaction> findAllByUser(User user, Pageable pageable);
    Page<Transaction> findAllByUserAndCategory(User user, Category category, Pageable pageable);
    Page<Transaction> findAllByUserAndDateBetween(
            User user, LocalDate startDate, LocalDate endDate, Pageable pageable
    );
    Page<Transaction> findAllByUserAndCategoryAndDateBetween(
            User user, Category category, LocalDate startDate, LocalDate endDate, Pageable pageable
    );
    boolean existsByCategory(Category category);
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.amount > 0")
    BigDecimal sumPositiveAmountByUser(User user);
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.amount < 0")
    BigDecimal sumNegativeAmountByUser(User user);
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user")
    BigDecimal sumAmountByUser(User user);
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
