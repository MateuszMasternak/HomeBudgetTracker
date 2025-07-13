package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Iterable<Transaction> findAllByUserSub(String userSub);

    Iterable<Transaction> findAllByAccount(Account account);

    Page<Transaction> findAllByAccount(Account account, Pageable pageable);

    Page<Transaction> findAllByAccountAndCategory(
            Account account, Category category, Pageable pageable);

    Page<Transaction> findAllByAccountAndDateBetween(
            Account account, LocalDate startDate, LocalDate endDate, Pageable pageable);

    Page<Transaction> findAllByAccountAndCategoryAndDateBetween(
            Account account, Category category, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userSub = :userSub " +
            "AND t.category = :category " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "AND t.amount > 0")
    Iterable<Transaction> findAllPositiveByUserSubAndCategoryAndDateBetween(
            String userSub, Category category, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userSub = :userSub " +
            "AND t.category = :category " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "AND t.amount < 0")
    Iterable<Transaction> findAllNegativeByUserSubAndCategoryAndDateBetween(
            String userSub, Category category, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userSub = :userSub " +
            "AND t.account = :account " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "AND t.amount > 0")
    Iterable<Transaction> findAllPositiveByUserSubAndAccountAndDateBetween(
            String userSub, Account account, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userSub = :userSub " +
            "AND t.account = :account " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "AND t.amount < 0")
    Iterable<Transaction> findAllNegativeByUserSubAndAccountAndDateBetween(
            String userSub, Account account, LocalDate startDate, LocalDate endDate);

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

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.amount > 0 AND t.account = :account AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumPositiveAmountByAccountAndDateBetween(
            Account account, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.amount > 0 AND t.account = :account AND t.category = :category"
            + " AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumPositiveAmountByAccountAndCategoryAndDateBetween(
            Account account, Category category, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.amount < 0 AND t.account = :account AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumNegativeAmountByAccountAndDateBetween(
            Account account, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.amount < 0 AND t.account = :account AND t.category = :category"
            + " AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumNegativeAmountByAccountAndCategoryAndDateBetween(
            Account account, Category category, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.account = :account AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByAccountAndDateBetween(
            Account account, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t " +
            "WHERE t.account = :account AND t.date <= :date")
    BigDecimal sumAmountByAccountToDate(
            Account account,LocalDate date);

    @Query("""
                SELECT new com.rainy.homebudgettracker.transaction.AccountBalance(
                    t.account.id,
                    SUM(t.amount)
                )
                FROM Transaction t
                WHERE t.account.userSub = :userSub
                GROUP BY t.account.id
            """)
    List<AccountBalance> getBalancesForUserAccounts(String userSub);
}
