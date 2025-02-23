package com.rainy.homebudgettracker.transaction;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ImportTransactionService {
    List<TransactionResponse> extractTransactions( MultipartFile file) throws IOException;
    boolean importTransactions(UUID accountId, List<TransactionRequest> transactions);
}
