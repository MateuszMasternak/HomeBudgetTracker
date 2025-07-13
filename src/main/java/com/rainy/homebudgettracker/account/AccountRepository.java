package com.rainy.homebudgettracker.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Iterable<Account> findAllByUserSub(String sub);

    @Modifying
    void deleteAllByUserSub(String sub);
}
