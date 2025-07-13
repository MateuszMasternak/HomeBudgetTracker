package com.rainy.homebudgettracker.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findAllByUserSub(String sub);

    @Modifying
    void deleteAllByUserSub(String sub);

    @Query("""
                SELECT new com.rainy.homebudgettracker.account.AccountWithBalance(
                    a,
                    SUM(t.amount)
                )
                FROM Account a
                LEFT JOIN Transaction t ON t.account = a
                WHERE a.id = :accountId
                GROUP BY a
            """)
    Optional<AccountWithBalance> findAccountWithBalanceById(UUID accountId);
}
