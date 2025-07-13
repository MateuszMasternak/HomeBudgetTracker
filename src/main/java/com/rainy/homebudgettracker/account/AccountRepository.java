package com.rainy.homebudgettracker.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findAllByUserSub(String sub);

    @Modifying
    void deleteAllByUserSub(String sub);
}
