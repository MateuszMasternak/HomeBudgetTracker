package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ImportTransactionService {
    List<TransactionResponse> extractTransactions( MultipartFile file) throws IOException;
    boolean importTransactions(UUID accountId, List<TransactionRequest> transactions)
            throws RecordDoesNotExistException, UserIsNotOwnerException;
}
