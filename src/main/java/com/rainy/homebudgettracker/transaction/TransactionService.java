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

import java.math.BigDecimal;
import java.time.LocalDate;

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
            String startDate,
            String endDate,
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
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
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
}
