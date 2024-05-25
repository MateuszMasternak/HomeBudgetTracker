package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.user.User;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    Iterable<Transaction> findAllByUser(User user);
    Iterable<Transaction> findAllByUserAndCategory(User user, Category category);
    Iterable<Transaction> findAllByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    Iterable<Transaction> findAllByUserAndCategoryAndDateBetween(
            User user, Category category, LocalDate startDate, LocalDate endDate
    );
}
