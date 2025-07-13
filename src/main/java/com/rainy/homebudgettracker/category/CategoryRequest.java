package com.rainy.homebudgettracker.category;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CategoryRequest {
    @NotEmpty(message = "Name is required")
    private String name;
}
