package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Iterable<Transaction> findAllByUserSub(String userSub);

    Page<Transaction> findAllByAccount(Account account, Pageable pageable);

    Page<Transaction> findAllByAccountAndCategory(
            Account account, Category category, Pageable pageable);

    Page<Transaction> findAllByAccountAndDateBetween(
            Account account, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Transaction> findAllByAccountAndCategoryAndDateBetween(
            Account account, Category category, LocalDate startDate, LocalDate endDate, Pageable pageable);

    boolean existsByCategory(Category category);

    void deleteAllByUserSub(String userSub);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.amount > 0 AND t.account = :account")
    BigDecimal sumPositiveAmountByAccount(Account account);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.amount < 0 AND t.account = :account")
    BigDecimal sumNegativeAmountByAccount(Account account);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.account = :account")
    BigDecimal sumAmountByAccount(Account account);

    Optional<Transaction> findById(UUID transactionId);

    void deleteById(UUID transactionId);
}
