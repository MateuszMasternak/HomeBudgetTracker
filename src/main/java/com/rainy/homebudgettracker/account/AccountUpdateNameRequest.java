package com.rainy.homebudgettracker.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class AccountUpdateNameRequest {
    @NotNull(message = "Id is required")
    private UUID id;
    @NotBlank(message = "Name is required")
    private String name;
}
