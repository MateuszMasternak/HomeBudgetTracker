package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public Page<TransactionResponse> findAllByUser(User user, Pageable pageable) {
        Page<Transaction> transactionResponses = transactionRepository.findAllByUser(user, pageable);
        return getTransactionResponses(transactionResponses);
    }

    public Page<TransactionResponse> findAllByUserAndCategory(User user, String categoryName, Pageable pageable)
            throws RecordDoesNotExistException
    {
        CategoryResponse categoryResponse = categoryService.findByUserAndName(user, categoryName);
        Category category = Category.builder()
                .id(categoryResponse.getId())
                .name(categoryResponse.getName())
                .user(user)
                .build();

        Page<Transaction> transactions = transactionRepository.findAllByUserAndCategory(user, category, pageable);
        return getTransactionResponses(transactions);
    }

    private Page<TransactionResponse> getTransactionResponses(Page<Transaction> transactions) {
        return transactions.map(transaction -> TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount().toString())
                .category(CategoryResponse.builder()
                        .id(transaction.getCategory().getId())
                        .name(transaction.getCategory().getName())
                        .build())
                .date(transaction.getDate().toString())
                .build());
    }

    private List<TransactionResponse> getTransactionResponses(Iterable<Transaction> transactions) {
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        transactions.forEach(transaction -> {
            TransactionResponse transactionResponse = TransactionResponse.builder()
                    .id(transaction.getId())
                    .amount(transaction.getAmount().toString())
                    .category(CategoryResponse.builder()
                            .id(transaction.getCategory().getId())
                            .name(transaction.getCategory().getName())
                            .build())
                    .date(transaction.getDate().toString())
                    .build();
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

    public Page<TransactionResponse> findAllByUserAndCategoryAndDateBetween(
            User user,
            String categoryName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) throws RecordDoesNotExistException {
        CategoryResponse categoryResponse = categoryService.findByUserAndName(user, categoryName);
        Category category = Category.builder()
                .id(categoryResponse.getId())
                .name(categoryResponse.getName())
                .user(user)
                .build();

        Page<Transaction> transactions = transactionRepository.findAllByUserAndCategoryAndDateBetween(
                user,
                category,
                startDate,
                endDate,
                pageable
        );
        return getTransactionResponses(transactions);
    }

    public TransactionResponse createTransaction(User user, TransactionRequest transactionRequest) throws RecordDoesNotExistException {
        CategoryResponse categoryResponse = categoryService.findByUserAndName(user,
                transactionRequest.getCategory().getName().toUpperCase());
        Category category = Category.builder()
                .id(categoryResponse.getId())
                .name(categoryResponse.getName())
                .user(user)
                .build();

        Transaction transaction = Transaction.builder()
                .category(category)
                .amount(transactionRequest.getAmount())
                .date(transactionRequest.getDate())
                .user(user)
                .build();

        transaction = transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount().toString())
                .category(CategoryResponse.builder()
                        .id(transaction.getCategory().getId())
                        .name(transaction.getCategory().getName())
                        .build())
                .date(transaction.getDate().toString())
                .build();
    }

    public void deleteTransaction(User user, Long transactionId) throws
            RecordDoesNotExistException,
            UserIsNotOwnerException
    {
        if (!transactionRepository.existsById(transactionId)) {
            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
        } else if (!transactionRepository.findById(transactionId).get().getUser().getEmail().equals(user.getEmail())) {
            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to user.");
        } else {
            transactionRepository.deleteById(transactionId);
        }
    }

    public boolean existsByCategory(Category category) {
        return transactionRepository.existsByCategory(category);
    }

    public String sumPositiveAmountByUser(User user) {
        BigDecimal sum = transactionRepository.sumPositiveAmountByUser(user);
        return sum == null ? "0" : sum.toString();
    }

    public String sumNegativeAmountByUser(User user) {
        BigDecimal sum = transactionRepository.sumNegativeAmountByUser(user);
        return sum == null ? "0" : sum.toString();
    }

    public String sumAmountByUser(User user) {
        BigDecimal sum = transactionRepository.sumAmountByUser(user);
        return sum == null ? "0" : sum.toString();
    }

    public String sumAmountByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate) {
        BigDecimal sum = transactionRepository.sumAmountByUserAndDateBetween(user, startDate, endDate);
        return sum == null ? "0" : sum.toString();
    }

    public byte[] generateCsvFileForUserTransactions(User user) throws IOException {
        Iterable<Transaction> transactions = transactionRepository.findAllByUser(user);
        List<TransactionResponse> transactionResponses = getTransactionResponses(transactions);

        Path csvFilePath = Paths.get("temp_transactions_" + user.getId() + "_" + LocalDate.now() + ".csv");

        try (FileWriter writer = new FileWriter(csvFilePath.toString())) {
            writer.append("sep=,\n"); // separator for microsoft excel
            writer.append("ID,Amount,Category,Date\n");
            for (TransactionResponse transactionResponse : transactionResponses) {
                writer.append(transactionResponse.getId().toString())
                        .append(",")
                        .append(transactionResponse.getAmount())
                        .append(",")
                        .append(transactionResponse.getCategory().getName())
                        .append(",")
                        .append(transactionResponse.getDate())
                        .append("\n");
            }
        }

        byte[] fileContent = Files.readAllBytes(csvFilePath);
        Files.delete(csvFilePath);

        return fileContent;
    }
}
