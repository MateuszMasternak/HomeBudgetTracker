package com.rainy.homebudgettracker.transaction.service.queryfilter;

import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.enums.AmountType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

import static com.rainy.homebudgettracker.transaction.service.queryfilter.TransactionSpecifications.*;

@Component
@RequiredArgsConstructor
public class TransactionSpecificationBuilder {

    public Specification<Transaction> build(AggregationFilter filter, String userSub) {
        return buildInternal(
                filter.accountId(),
                filter.categoryId(),
                filter.startDate(),
                filter.endDate(),
                filter.amountType(),
                userSub
        );
    }

    public Specification<Transaction> build(TransactionFilter filter, String userSub) {
        return buildInternal(
                filter.accountId(),
                filter.categoryId(),
                filter.startDate(),
                filter.endDate(),
                null, // AmountType is not used in TransactionFilter
                userSub
        );
    }

    private Specification<Transaction> buildInternal(
            UUID accountId,
            UUID categoryId,
            LocalDate startDate,
            LocalDate endDate,
            AmountType amountType,
            String userSub
    ) {
        Specification<Transaction> spec = Specification.where(byUserSub(userSub));

        if (accountId != null) {
            spec = spec.and(byAccountId(accountId));
        }
        if (categoryId != null) {
            spec = spec.and(byCategoryId(categoryId));
        }
        if (startDate != null && endDate != null) {
            spec = spec.and(betweenDates(startDate, endDate));
        }
        if (amountType != null) {
            spec = spec.and(byAmountType(amountType));
        }

        return spec;
    }
}
