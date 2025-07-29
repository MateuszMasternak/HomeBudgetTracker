package com.rainy.homebudgettracker.exchange.nbp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record NbpExchangeRateResponse(
        @JsonProperty("table") String table,
        @JsonProperty("no") String no,
        @JsonProperty("effectiveDate") String effectiveDate,
        @JsonProperty("rates") List<NbpRate> rates
) {}
