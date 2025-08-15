package com.rainy.homebudgettracker.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RuleRequest(
        @NotBlank(message = "Keyword is required")
//        @Size(max = 255, message = "Keyword can't be longer than 255 characters")
        String keyword,
        @NotNull(message = "Category name is required")
        String categoryName
) {}
