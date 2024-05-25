package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public Iterable<Transaction> findAllByUser(User user) {
        return transactionRepository.findAllByUser(user);
    }

    public Iterable<Transaction> findAllByUserAndCategory(User user, Category category) {
        return transactionRepository.findAllByUserAndCategory(user, category);
    }

    public Iterable<Transaction> findAllByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findAllByUserAndDateBetween(user, startDate, endDate);
    }

    public Iterable<Transaction> findAllByUserAndCategoryAndDateBetween(
            User user,
            Category category,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return transactionRepository.findAllByUserAndCategoryAndDateBetween(user, category, startDate, endDate);
    }

    public Transaction createTransaction(User user, TransactionRequest transactionRequest) {
        Transaction transaction = Transaction.builder()
                .category(transactionRequest.getCategory())
                .amount(transactionRequest.getAmount())
                .date(transactionRequest.getDate())
                .user(user)
                .build();

        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(User user, Long transactionId) throws
            RecordDoesNotExistException,
            UserIsNotOwnerException
    {
        if (!transactionRepository.existsById(transactionId)) {
            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
        } else if (!transactionRepository.findById(transactionId).get().getUser().equals(user)) {
            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to user.");
        } else {
            transactionRepository.deleteById(transactionId);
        }
    }
}
