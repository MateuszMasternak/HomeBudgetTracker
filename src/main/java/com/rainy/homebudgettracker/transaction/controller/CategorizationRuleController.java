package com.rainy.homebudgettracker.transaction.controller;

import com.rainy.homebudgettracker.transaction.dto.RuleRequest;
import com.rainy.homebudgettracker.transaction.dto.RuleResponse;
import com.rainy.homebudgettracker.transaction.service.CategorizationRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categorization-rules")
@RequiredArgsConstructor
public class CategorizationRuleController {

    private final CategorizationRuleService ruleService;

    @GetMapping
    public ResponseEntity<List<RuleResponse>> getUserRules() {
        return ResponseEntity.ok(ruleService.getUserRules());
    }

    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@RequestBody RuleRequest request) {
        return ResponseEntity.ok(ruleService.createRule(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
