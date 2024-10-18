package com.rainy.homebudgettracker.mapper;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountRequest;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.images.ImageService;
import com.rainy.homebudgettracker.transaction.SumResponse;
import com.rainy.homebudgettracker.user.UserService;
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
    private final UserService userService;
    private final ImageService imageService;

    // DOESN'T INCLUDE MAPPING TransactionRequest TO Transaction
    @SuppressWarnings("unchecked")
    public <T> T map(Object source, Class<T> destinationType) {
        return switch (destinationType.getSimpleName()) {
            case "TransactionResponse": {
                if (source instanceof Transaction transaction)
                    yield (T) mapTransactionToResponse(transaction);
                else
                    throw new UnsupportedOperationException("Mapping not supported");
            }
            case "CategoryResponse": {
                if (source instanceof Category category)
                    yield (T) mapCategoryToResponse(category);
                else
                    throw new UnsupportedOperationException("Mapping not supported");
            }
            case "Category": {
                if (source instanceof CategoryRequest categoryRequest)
                    yield (T) mapCategoryRequestToCategory(categoryRequest);
                else
                    throw new UnsupportedOperationException("Mapping not supported");
            }
            case "AccountResponse": {
                if (source instanceof Account account)
                    yield (T) mapAccountToResponse(account);
                else
                    throw new UnsupportedOperationException("Mapping not supported");
            }
            case "Account": {
                if (source instanceof AccountRequest accountRequest)
                    yield (T) mapAccountRequestToAccount(accountRequest);
                else
                    throw new UnsupportedOperationException("Mapping not supported");
            }
            case "SumResponse": {
                if (source instanceof BigDecimal sum) {
                    yield (T) SumResponse.builder().amount(sum.toString()).build();
                }
                else
                    throw new UnsupportedOperationException("Mapping not supported");
            }
            default:
                throw new UnsupportedOperationException("Mapping not supported");
        };
    }

    public Transaction mapTransactionRequestToTransaction(
            TransactionRequest transactionRequest, Account account, Category category) {
        return Transaction.builder()
                .amount(transactionRequest.getAmount().setScale(2, RoundingMode.HALF_UP))
                .category(category)
                .date(transactionRequest.getDate())
                .account(account)
                .paymentMethod(transactionRequest.getPaymentMethod())
                .userSub(userService.getUserSub())
                .details(transactionRequest.getDetails())
                .build();
    }

    private TransactionResponse mapTransactionToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(String.valueOf(transaction.getAmount().setScale(2, RoundingMode.HALF_UP)))
                .category(mapCategoryToResponse(transaction.getCategory()))
                .date(String.valueOf(transaction.getDate()))
                .account(mapAccountToResponse(transaction.getAccount()))
                .paymentMethod(transaction.getPaymentMethod().name())
                .imageUrl(imageService.getImageUrl(transaction))
                .details(transaction.getDetails())
                .build();
    }

    private CategoryResponse mapCategoryToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    private Category mapCategoryRequestToCategory(CategoryRequest categoryRequest) {
        return Category.builder()
                .name(categoryRequest.getName())
                .userSub(userService.getUserSub())
                .build();
    }

    private AccountResponse mapAccountToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .currencyCode(account.getCurrencyCode().name())
                .build();
    }

    private Account mapAccountRequestToAccount(AccountRequest accountRequest) {
        return Account.builder()
                .name(accountRequest.getName())
                .currencyCode(accountRequest.getCurrencyCode())
                .userSub(userService.getUserSub())
                .build();
    }
}
