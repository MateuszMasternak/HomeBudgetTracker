package com.rainy.homebudgettracker.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class AccountUpdateNameRequest {
    @NotNull(message = "Id is required")
    private Long id;
    @NotEmpty(message = "Name is required")
    @NotBlank(message = "Name is required")
    private String name;
}
