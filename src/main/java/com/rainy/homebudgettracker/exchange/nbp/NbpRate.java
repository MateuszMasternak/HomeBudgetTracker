package com.rainy.homebudgettracker.exchange.nbp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record NbpRate(
        @JsonProperty("currency") String currency,
        @JsonProperty("code") String code,
        @JsonProperty("mid") BigDecimal mid
) {}
