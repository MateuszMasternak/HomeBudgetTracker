package com.rainy.homebudgettracker.transaction.repository;

import com.rainy.homebudgettracker.transaction.CategorizationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategorizationRuleRepository extends JpaRepository<CategorizationRule, UUID> {
    List<CategorizationRule> findByUserSub(String userSub);
}