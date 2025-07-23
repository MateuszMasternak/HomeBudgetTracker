package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.Category;

import java.math.BigDecimal;

public record CategorySum(
        Category category,
        BigDecimal sum
) {}
