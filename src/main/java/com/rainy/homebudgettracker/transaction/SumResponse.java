package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.CategoryResponse;
import lombok.*;

@Builder
public record SumResponse(
    String amount,
    CategoryResponse category
) {}
