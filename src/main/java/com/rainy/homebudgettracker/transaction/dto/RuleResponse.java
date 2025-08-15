package com.rainy.homebudgettracker.transaction.dto;

import com.rainy.homebudgettracker.category.CategoryResponse;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RuleResponse(
        UUID id,
        String keyword,
        CategoryResponse category
) {}
