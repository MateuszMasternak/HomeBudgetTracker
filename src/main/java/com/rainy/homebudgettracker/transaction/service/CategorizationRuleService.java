package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.transaction.dto.RuleRequest;
import com.rainy.homebudgettracker.transaction.dto.RuleResponse;

import java.util.List;
import java.util.UUID;

public interface CategorizationRuleService {
    List<RuleResponse> getUserRules();
    RuleResponse createRule(RuleRequest request);
    void deleteRule(UUID ruleId);
}
