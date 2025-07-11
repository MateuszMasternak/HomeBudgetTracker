package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.handler.exception.WrongFileFormatException;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ImportTransactionService {
    List<TransactionResponse> extractTransactions(MultipartFile file, BankName bankName) throws IOException, WrongFileFormatException;
    boolean importTransactions(UUID accountId, List<TransactionRequest> transactions)
            throws RecordDoesNotExistException, UserIsNotOwnerException;
}
