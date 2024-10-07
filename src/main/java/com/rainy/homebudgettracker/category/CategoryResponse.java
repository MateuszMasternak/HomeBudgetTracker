package com.rainy.homebudgettracker.category;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CategoryResponse {
    private Long id;
    private String name;
}
