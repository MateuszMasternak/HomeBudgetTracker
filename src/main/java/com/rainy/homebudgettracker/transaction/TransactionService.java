package com.rainy.homebudgettracker.transaction;

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
}
