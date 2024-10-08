package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
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
    Page<Transaction> findAllByUserAndAccount(User user, Account account, Pageable pageable);
    Page<Transaction> findAllByUserAndAccountAndCategory(
            User user, Account account, Category category, Pageable pageable
    );
    Page<Transaction> findAllByUserAndAccountAndDateBetween(
            User user, Account account, LocalDate startDate, LocalDate endDate, Pageable pageable
    );
    Page<Transaction> findAllByUserAndAccountAndCategoryAndDateBetween(
            User user, Account account, Category category, LocalDate startDate, LocalDate endDate,
            Pageable pageable
    );

    boolean existsByCategory(Category category);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user AND t.amount > 0 AND t.account = :account")
    BigDecimal sumPositiveAmountByUserAndAccount(User user, Account account);
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user AND t.amount < 0 AND t.account = :account")
    BigDecimal sumNegativeAmountByUserAndAccount(User user, Account account);
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.user = :user AND t.account = :account")
    BigDecimal sumAmountByUserAndAccount(User user, Account account);

    void deleteAllByUser(User user);
}
