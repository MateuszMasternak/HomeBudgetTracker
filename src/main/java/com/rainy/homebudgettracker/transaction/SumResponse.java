package com.rainy.homebudgettracker.transaction;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SumResponse {
    private String amount;
}
