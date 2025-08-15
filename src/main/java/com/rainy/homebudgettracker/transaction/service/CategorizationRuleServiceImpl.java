package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRepository;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.CategorizationRule;
import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.dto.RuleRequest;
import com.rainy.homebudgettracker.transaction.dto.RuleResponse;
import com.rainy.homebudgettracker.transaction.repository.CategorizationRuleRepository;
import com.rainy.homebudgettracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategorizationRuleServiceImpl implements CategorizationRuleService {

    private final CategorizationRuleRepository ruleRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RuleResponse> getUserRules() {
        String userSub = userService.getUserSub();
        return ruleRepository.findByUserSub(userSub).stream()
                .map(rule -> modelMapper.map(rule, RuleResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RuleResponse createRule(RuleRequest request) {
        String userSub = userService.getUserSub();

        Category category = categoryRepository.findByUserSubAndName(userSub, request.categoryName())
            .orElseThrow(() -> new RecordDoesNotExistException("Category " + request.categoryName() + " does not exist"));

        CategorizationRule newRule = modelMapper.map(request, CategorizationRule.class, userSub, category);
        CategorizationRule savedRule = ruleRepository.save(newRule);

        return modelMapper.map(savedRule, RuleResponse.class);
    }

    @Override
    @Transactional
    public void deleteRule(UUID ruleId) {
        CategorizationRule ruleToDelete = findAndVerifyRuleOwner(ruleId);
        ruleRepository.delete(ruleToDelete);
    }

    private CategorizationRule findAndVerifyRuleOwner(UUID ruleId) {
        String userSub = userService.getUserSub();
        return ruleRepository.findById(ruleId)
                .map(transaction -> {
                    if (!transaction.getUserSub().equals(userSub)) {
                        throw new UserIsNotOwnerException("Rule with id " + ruleId
                                + " does not belong to the user.");
                    }
                    return transaction;
                })
                .orElseThrow(() -> new RecordDoesNotExistException("Transaction with id "
                        + ruleId + " does not exist."));
    }
}
