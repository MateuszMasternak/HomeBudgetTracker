package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.category.CategoryRepository;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.handler.exception.FileProcessingException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.CategorizationRule;
import com.rainy.homebudgettracker.transaction.dto.TransactionRequest;
import com.rainy.homebudgettracker.transaction.dto.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;
import com.rainy.homebudgettracker.transaction.repository.CategorizationRuleRepository;
import com.rainy.homebudgettracker.transaction.service.extractor.TransactionExtractor;
import com.rainy.homebudgettracker.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImportTransactionServiceImpl implements ImportTransactionService {
    private final TransactionService transactionService;
    private final List<TransactionExtractor> extractors;
    private final CategorizationRuleRepository ruleRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<TransactionResponse> extractTransactions(MultipartFile file, BankName bankName) {
        TransactionExtractor extractor = extractors.stream()
                .filter(e -> e.supports(bankName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported bank name: " + bankName));

        try {
            List<TransactionResponse> extractedTransactions = extractor.extract(file.getInputStream());
            List<CategorizationRule> rules = ruleRepository.findByUserSub(userService.getUserSub());

            return extractedTransactions.stream()
                    .map(transaction -> applyRules(transaction, rules))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error while extracting transactions from file: {}", file.getOriginalFilename(), e);
            throw new FileProcessingException("Error while extracting data from file: " + file.getOriginalFilename());
        }
    }

    @Override
    public boolean importTransactions(UUID accountId, List<TransactionRequest> transactions) {
        for (TransactionRequest transaction : transactions) {
            transactionService.createTransactionForCurrentUser(accountId, transaction);
        }

        return true;
    }

    private TransactionResponse applyRules(TransactionResponse transaction, List<CategorizationRule> rules) {
        for (CategorizationRule rule : rules) {
            if (transaction.details().toLowerCase().contains(rule.getKeyword().toLowerCase())) {
                return new TransactionResponse(
                        transaction.id(),
                        transaction.amount(),
                        modelMapper.map(rule.getCategory(), CategoryResponse.class),
                        transaction.date(),
                        null,
                        transaction.transactionMethod(),
                        false,
                        transaction.details()
                );
            }
        }

        return transaction;
    }
}