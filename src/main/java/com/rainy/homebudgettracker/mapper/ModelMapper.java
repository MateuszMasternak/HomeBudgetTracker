package com.rainy.homebudgettracker.mapper;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountRequest;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.transaction.SumResponse;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class ModelMapper {
    @SuppressWarnings("unchecked")
    public <T> T map(Object source, Class<T> destinationType, Object... args) {
        final String message = "Mapping not supported";
        return switch (destinationType.getSimpleName()) {
            case "Transaction": {
                if (source instanceof TransactionRequest transactionRequest
                        && args.length == 3
                        && args[0] instanceof String userSub
                        && args[1] instanceof Category category
                        && args[2] instanceof Account account)
                    yield (T) mapTransactionRequestToTransaction(transactionRequest, userSub, category, account);
                else
                    throw new UnsupportedOperationException(message);
            }
            case "TransactionResponse": {
                if (source instanceof Transaction transaction
                        && args.length == 1
                        && args[0] instanceof String imageUrl)
                    yield (T) mapTransactionToResponse(transaction, imageUrl);
                else
                    throw new UnsupportedOperationException(message);
            }
            case "CategoryResponse": {
                if (source instanceof Category category && args.length == 0)
                    yield (T) mapCategoryToResponse(category);
                else
                    throw new UnsupportedOperationException(message);
            }
            case "Category": {
                if (source instanceof CategoryRequest categoryRequest
                        && args.length == 1
                        && args[0] instanceof String userSub)
                    yield (T) mapCategoryRequestToCategory(categoryRequest, userSub);
                else
                    throw new UnsupportedOperationException(message);
            }
            case "AccountResponse": {
                if (source instanceof Account account && args.length == 0)
                    yield (T) mapAccountToResponse(account);
                else if (source instanceof Account account
                        && args.length == 1
                        && args[0] instanceof BigDecimal balance)
                    yield (T) mapAccountToResponse(account, balance);
                else
                    throw new UnsupportedOperationException(message);
            }
            case "Account": {
                if (source instanceof AccountRequest accountRequest
                        && args.length == 1
                        && args[0] instanceof String userSub)
                    yield (T) mapAccountRequestToAccount(accountRequest, userSub);
                else
                    throw new UnsupportedOperationException(message);
            }
            case "SumResponse": {
                if (source instanceof BigDecimal sum) {
                    yield (T) mapBigDecimalToSumResponse(sum);
                }
                else
                    throw new UnsupportedOperationException(message);
            }
            default:
                throw new UnsupportedOperationException(message);
        };
    }

    private Transaction mapTransactionRequestToTransaction(
            TransactionRequest transactionRequest, String userSub, Category category, Account account) {
        return Transaction.builder()
                .amount(transactionRequest.getAmount().setScale(2, RoundingMode.HALF_UP))
                .category(category)
                .date(transactionRequest.getDate())
                .account(account)
                .paymentMethod(transactionRequest.getPaymentMethod())
                .userSub(userSub)
                .details(transactionRequest.getDetails())
                .build();
    }

    private TransactionResponse mapTransactionToResponse(Transaction transaction, String imageUrl) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(String.valueOf(transaction.getAmount()))
                .category(mapCategoryToResponse(transaction.getCategory()))
                .date(String.valueOf(transaction.getDate()))
                .account(mapAccountToResponse(transaction.getAccount()))
                .paymentMethod(transaction.getPaymentMethod().name())
                .imageUrl(imageUrl)
                .details(transaction.getDetails())
                .build();
    }

    private CategoryResponse mapCategoryToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    private Category mapCategoryRequestToCategory(CategoryRequest categoryRequest, String userSub) {
        return Category.builder()
                .name(categoryRequest.getName())
                .userSub(userSub)
                .build();
    }

    private AccountResponse mapAccountToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .currencyCode(account.getCurrencyCode().name())
                .build();
    }

    private AccountResponse mapAccountToResponse(Account account, BigDecimal balance) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .currencyCode(account.getCurrencyCode().name())
                .balance(String.valueOf(balance))
                .build();
    }

    private Account mapAccountRequestToAccount(AccountRequest accountRequest, String userSub) {
        return Account.builder()
                .name(accountRequest.getName())
                .currencyCode(accountRequest.getCurrencyCode())
                .userSub(userSub)
                .build();
    }

    private SumResponse mapBigDecimalToSumResponse(BigDecimal sum) {
        return SumResponse.builder()
                .amount(String.valueOf(sum))
                .build();
    }
}
