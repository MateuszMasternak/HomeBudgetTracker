package com.rainy.homebudgettracker.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Iterable<Account> findAllByUserSub(String sub);

    @Modifying
    @Query("update Account a set a.name = :name where a.id = :id")
    void updateAccountName(UUID id, String name);

    void deleteAllByUserSub(String sub);

    Optional<Account> findById(UUID id);

    Boolean existsById(UUID id);
}
