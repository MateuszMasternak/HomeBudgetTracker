package com.rainy.homebudgettracker.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryRequest {
    @NotEmpty(message = "Name is required")
    @NotBlank(message = "Name is required")
    private String name;
}
