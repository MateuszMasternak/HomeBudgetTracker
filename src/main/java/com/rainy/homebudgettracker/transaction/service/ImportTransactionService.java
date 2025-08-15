package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.transaction.dto.TransactionRequest;
import com.rainy.homebudgettracker.transaction.dto.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ImportTransactionService {
    List<TransactionResponse> extractTransactions(MultipartFile file, BankName bankName);
    boolean importTransactions(UUID accountId, List<TransactionRequest> transactions);
}
