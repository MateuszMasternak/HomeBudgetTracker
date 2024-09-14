package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.account.AccountService;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final ExchangeService exchangeService;

    public Page<TransactionResponse> findAllByUser(User user, Pageable pageable) {
        Page<Transaction> transactionResponses = transactionRepository.findAllByUser(user, pageable);
        return getTransactionResponses(transactionResponses);
    }

    public Page<TransactionResponse> findAllByUserAndCurrencyCode(User user, CurrencyCode currencyCode, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findAllByUserAndCurrencyCode(user, currencyCode, pageable);
        return getTransactionResponses(transactions);
    }

    public Page<TransactionResponse> findAllByUserAndCategory(User user, String categoryName, Pageable pageable)
            throws RecordDoesNotExistException
    {
        CategoryResponse categoryResponse = categoryService.findByUserAndName(user, categoryName);
        Category category = getCategory(categoryResponse, user);

        Page<Transaction> transactions = transactionRepository.findAllByUserAndCategory(user, category, pageable);
        return getTransactionResponses(transactions);
    }

    public Page<TransactionResponse> findAllByUserAndCurrencyCodeAndCategory(
            User user, CurrencyCode currencyCode, String categoryName, Pageable pageable
    ) throws RecordDoesNotExistException {
        CategoryResponse categoryResponse = categoryService.findByUserAndName(user, categoryName);
        Category category = getCategory(categoryResponse, user);

        Page<Transaction> transactions = transactionRepository.findAllByUserAndCurrencyCodeAndCategory(
                user, currencyCode, category, pageable
        );
        return getTransactionResponses(transactions);
    }

    private Page<TransactionResponse> getTransactionResponses(Page<Transaction> transactions) {
        return transactions.map(this::getTransactionResponse);
    }

    private List<TransactionResponse> getTransactionResponses(Iterable<Transaction> transactions) {
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        transactions.forEach(transaction -> {
            TransactionResponse transactionResponse = getTransactionResponse(transaction);
            transactionResponses.add(transactionResponse);
        });

        return transactionResponses;
    }

    public Page<TransactionResponse> findAllByUserAndDateBetween(
            User user, LocalDate startDate, LocalDate endDate, Pageable pageable
    ) {
        Page<Transaction> transactions = transactionRepository.findAllByUserAndDateBetween(
                user, startDate, endDate, pageable);
        return getTransactionResponses(transactions);
    }

    public Page<TransactionResponse> findAllByUserAndCurrencyCodeAndDateBetween(
            User user,
            CurrencyCode currencyCode,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        Page<Transaction> transactions = transactionRepository.findAllByUserAndCurrencyCodeAndDateBetween(
                user,
                currencyCode,
                startDate,
                endDate,
                pageable
        );
        return getTransactionResponses(transactions);
    }

    public Page<TransactionResponse> findAllByUserAndCategoryAndDateBetween(
            User user,
            String categoryName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) throws RecordDoesNotExistException {
        CategoryResponse categoryResponse = categoryService.findByUserAndName(user, categoryName);
        Category category = getCategory(categoryResponse, user);

        Page<Transaction> transactions = transactionRepository.findAllByUserAndCategoryAndDateBetween(
                user,
                category,
                startDate,
                endDate,
                pageable
        );
        return getTransactionResponses(transactions);
    }

    public Page<TransactionResponse> findAllByUserAndCurrencyCodeAndCategoryAndDateBetween(
            User user,
            CurrencyCode currencyCode,
            String categoryName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) throws RecordDoesNotExistException {
        CategoryResponse categoryResponse = categoryService.findByUserAndName(user, categoryName);
        Category category = getCategory(categoryResponse, user);

        Page<Transaction> transactions = transactionRepository.findAllByUserAndCurrencyCodeAndCategoryAndDateBetween(
                user,
                currencyCode,
                category,
                startDate,
                endDate,
                pageable
        );
        return getTransactionResponses(transactions);
    }

    @Transactional
    public TransactionResponse createTransaction(User user, TransactionRequest transactionRequest)
            throws RecordDoesNotExistException
    {
        return saveTransactionResponse(user, transactionRequest);
    }

    @Transactional
    public TransactionResponse createTransaction(User user, CurrencyCode targetCurrency, String exchangeRate,
                                                 TransactionRequest transactionRequest)
            throws RecordDoesNotExistException
    {
        if (exchangeRate != null) {
            updateTransactionAmount(transactionRequest, exchangeRate);
        } else {
            ExchangeResponse exchangeResponse = exchangeService.getExchangeRate(
                    transactionRequest.getCurrencyCode(),
                    targetCurrency.toString()
            );
            String apiExchangeRate = exchangeResponse.getConversionRate();
            updateTransactionAmount(transactionRequest, apiExchangeRate);
        }
        transactionRequest.setCurrencyCode(targetCurrency.toString());

        return saveTransactionResponse(user, transactionRequest);
    }

    private TransactionResponse saveTransactionResponse(User user, TransactionRequest transactionRequest)
            throws RecordDoesNotExistException
    {
        CategoryResponse categoryResponse = categoryService.findByUserAndName(user,
                transactionRequest.getCategory().getName().toUpperCase());
        Category category = getCategory(categoryResponse, user);

        Transaction transaction = getTransaction(transactionRequest, user, category);

        AccountResponse account = accountService.findByUserAndCurrencyCode(user, transaction.getCurrencyCode());
        accountService.updateAccountBalance(
                user,
                transaction.getAmount(),
                CurrencyCode.valueOf(account.getCurrencyCode()));

        transaction = transactionRepository.save(transaction);

        return getTransactionResponse(transaction);
    }

    private void updateTransactionAmount(TransactionRequest transaction, String rate) {
        transaction.setAmount(
                transaction.getAmount()
                        .multiply(BigDecimal.valueOf(Double.parseDouble(rate)))
        );
    }

    private TransactionResponse getTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount().toString())
                .category(CategoryResponse.builder()
                        .id(transaction.getCategory().getId())
                        .name(transaction.getCategory().getName())
                        .build())
                .date(transaction.getDate().toString())
                .currencyCode(transaction.getCurrencyCode().toString())
                .build();
    }

    private Category getCategory(CategoryResponse categoryResponse, User user) {
        return Category.builder()
                .id(categoryResponse.getId())
                .name(categoryResponse.getName())
                .user(user)
                .build();
    }

    private Transaction getTransaction(TransactionRequest transactionRequest, User user, Category category) {
        return Transaction.builder()
                .category(category)
                .amount(transactionRequest.getAmount())
                .date(transactionRequest.getDate())
                .user(user)
                .currencyCode(CurrencyCode.valueOf(transactionRequest.getCurrencyCode()))
                .build();
    }

    public void deleteTransaction(User user, Long transactionId) throws
            RecordDoesNotExistException,
            UserIsNotOwnerException
    {
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isEmpty()) {
            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
        } else if (!transaction.get().getUser().getEmail().equals(user.getEmail())) {
            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to user.");
        } else {
            transactionRepository.deleteById(transactionId);
        }
    }

    public boolean existsByCategory(Category category) {
        return transactionRepository.existsByCategory(category);
    }

    public SumResponse sumPositiveAmountByUser(User user, CurrencyCode currencyCode) {
        BigDecimal sum = transactionRepository.sumPositiveAmountByUser(user, currencyCode);
        String amount = sum == null ? "0" : sum.toString();
        return SumResponse.builder().amount(amount).build();
    }

    public SumResponse sumNegativeAmountByUser(User user, CurrencyCode currencyCode) {
        BigDecimal sum = transactionRepository.sumNegativeAmountByUser(user, currencyCode);
        String amount = sum == null ? "0" : sum.toString();
        return SumResponse.builder().amount(amount).build();
    }

    public SumResponse sumAmountByUser(User user, CurrencyCode currencyCode) {
        BigDecimal sum = transactionRepository.sumAmountByUser(user, currencyCode);
        String amount = sum == null ? "0" : sum.toString();
        return SumResponse.builder().amount(amount).build();
    }

    public SumResponse sumAmountByUserAndDateBetween(
            User user, CurrencyCode currencyCode, LocalDate startDate, LocalDate endDate)
    {
        BigDecimal sum = transactionRepository.sumAmountByUserAndDateBetween(user, currencyCode, startDate, endDate);
        String amount = sum == null ? "0" : sum.toString();
        return SumResponse.builder().amount(amount).build();
    }

    public byte[] generateCsvFileForUserTransactions(User user) throws IOException {
        Iterable<Transaction> transactions = transactionRepository.findAllByUser(user);
        List<TransactionResponse> transactionResponses = getTransactionResponses(transactions);

        Path csvFilePath = Paths.get("temp_transactions_" + user.getId() + "_" + LocalDate.now() + ".csv");

        try (FileWriter writer = new FileWriter(csvFilePath.toString())) {
            writer.append("sep=,\n"); // separator for microsoft excel
            writer.append("ID,Amount,Category,Date,Currency code\n");
            for (TransactionResponse transactionResponse : transactionResponses) {
                writer.append(transactionResponse.getId().toString())
                        .append(",")
                        .append(transactionResponse.getAmount())
                        .append(",")
                        .append(transactionResponse.getCategory().getName())
                        .append(",")
                        .append(transactionResponse.getDate())
                        .append(",")
                        .append(transactionResponse.getCurrencyCode())
                        .append("\n");
            }
        }

        byte[] fileContent = Files.readAllBytes(csvFilePath);
        Files.delete(csvFilePath);

        return fileContent;
    }
}
