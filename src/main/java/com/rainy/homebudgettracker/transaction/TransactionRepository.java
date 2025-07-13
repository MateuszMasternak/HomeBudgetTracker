package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    boolean existsByCategory(Category category);
    @Modifying
    void deleteAllByUserSub(String userSub);
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
