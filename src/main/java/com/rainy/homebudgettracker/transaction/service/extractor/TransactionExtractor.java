package com.rainy.homebudgettracker.transaction.service.extractor;

import com.rainy.homebudgettracker.transaction.dto.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;

import java.io.InputStream;
import java.util.List;

public interface TransactionExtractor {
    boolean supports(BankName bankName);
    List<TransactionResponse> extract(InputStream inputStream);
}
