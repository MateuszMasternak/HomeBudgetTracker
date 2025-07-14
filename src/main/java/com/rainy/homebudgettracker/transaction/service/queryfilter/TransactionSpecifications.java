package com.rainy.homebudgettracker.transaction.service.queryfilter;

import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.enums.AmountType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class TransactionSpecifications {
    private TransactionSpecifications() {}

    public static Specification<Transaction> byAccountId(UUID accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID must not be null");
        }

        return (root, query, cb) -> cb.equal(root.get("account").get("id"), accountId);
    }

    public static Specification<Transaction> byCategoryId(UUID categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID must not be null");
        }
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Transaction> betweenDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date must not be null");
        }
        return (root, query, cb) -> cb.between(root.get("date"), startDate, endDate);
    }

    public static Specification<Transaction> byUserSub(String userSub) {
        if (userSub == null || userSub.isEmpty()) {
            throw new IllegalArgumentException("User sub must not be null or empty");
        }

        return (root, query, cb) -> cb.equal(root.get("userSub"), userSub);
    }

    public static Specification<Transaction> byAmountType(AmountType amountType) {
        if (amountType == null) {
            throw new IllegalArgumentException("Amount type must not be null");
        }

        return (root, query, cb) -> switch (amountType) {
            case POSITIVE -> cb.greaterThan(root.get("amount"), 0);
            case NEGATIVE -> cb.lessThan(root.get("amount"), 0);
            case ALL -> cb.conjunction();
        };
    }
}
