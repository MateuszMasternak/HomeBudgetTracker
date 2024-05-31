package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public Iterable<TransactionResponse> findAllByUser(User user) {
        Iterable<Transaction> transactions = transactionRepository.findAllByUser(user);
        return getTransactionResponses(transactions);
    }

    public Iterable<TransactionResponse> findAllByUserAndCategory(User user, String categoryName)
            throws RecordDoesNotExistException
    {
        CategoryResponse categoryResponse = categoryService.findByUserAndName(user, categoryName);
        Category category = Category.builder()
                .id(categoryResponse.getId())
                .name(categoryResponse.getName())
                .user(user)
                .build();


        Iterable<Transaction> transactions = transactionRepository.findAllByUserAndCategory(user, category);
        return getTransactionResponses(transactions);
    }

    private Iterable<TransactionResponse> getTransactionResponses(Iterable<Transaction> transactions) {
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        transactions.forEach(transaction -> {
            Category category = transaction.getCategory();
            transactionResponses.add(TransactionResponse.builder()
                    .id(transaction.getId())
                    .amount(transaction.getAmount().toString())
                    .category(CategoryResponse.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .build())
                    .date(transaction.getDate().toString())
                    .build()
            );
        });

        return transactionResponses;
    }

    public Iterable<TransactionResponse> findAllByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate) {
        Iterable<Transaction> transactions = transactionRepository.findAllByUserAndDateBetween(user, startDate, endDate);
        return getTransactionResponses(transactions);
    }

    public Iterable<TransactionResponse> findAllByUserAndCategoryAndDateBetween(
            User user,
            String categoryName,
            String startDate,
            String endDate
    ) throws RecordDoesNotExistException {
        CategoryResponse categoryResponse = categoryService.findByUserAndName(user, categoryName);
        Category category = Category.builder()
                .id(categoryResponse.getId())
                .name(categoryResponse.getName())
                .user(user)
                .build();

        Iterable<Transaction> transactions = transactionRepository.findAllByUserAndCategoryAndDateBetween(
                user,
                category,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );

        List<TransactionResponse> transactionResponses = new ArrayList<>();
        transactions.forEach(transaction -> transactionResponses.add(TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount().toString())
                .category(
                        CategoryResponse.builder()
                                .id(transaction.getCategory().getId())
                                .name(transaction.getCategory().getName())
                                .build())
                .date(transaction.getDate().toString())
                .build()
        ));

        return transactionResponses;
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
}
