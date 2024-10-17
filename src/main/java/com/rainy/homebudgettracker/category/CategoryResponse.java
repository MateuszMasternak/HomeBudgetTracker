package com.rainy.homebudgettracker.category;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CategoryResponse {
    private UUID id;
    private String name;
}
