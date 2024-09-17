package com.rainy.homebudgettracker.helpers;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountRequest;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.account.AccountService;
import com.rainy.homebudgettracker.auth.UserDetailsServiceImpl;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelMapper {
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final UserDetailsServiceImpl userDetailsService;

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
            default:
                throw new UnsupportedOperationException("Mapping not supported");
        };
    }

    @SuppressWarnings("unchecked")
    public <T> T map(Object source, Class<T> destinationType, boolean mayThrowRDNEException)
            throws RecordDoesNotExistException
    {
        if (!mayThrowRDNEException) {
            return map(source, destinationType);
        }

        return switch (destinationType.getSimpleName()) {
            case "Transaction": {
                if (source instanceof TransactionRequest transactionRequest)
                    yield (T) mapTransactionRequestToTransaction(transactionRequest);
                else
                    throw new UnsupportedOperationException("Mapping not supported");
            }
            default:
                throw new UnsupportedOperationException("Mapping not supported");
        };
    }

    private TransactionResponse mapTransactionToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(String.valueOf(transaction.getAmount()))
                .category(mapCategoryToResponse(transaction.getCategory()))
                .date(String.valueOf(transaction.getDate()))
                .account(mapAccountToResponse(transaction.getAccount()))
                .paymentMethod(transaction.getPaymentMethod().name())
                .build();
    }

    private Transaction mapTransactionRequestToTransaction(TransactionRequest transactionRequest)
            throws RecordDoesNotExistException
    {
        Category category = categoryService.findOneByCurrentUserAndName(transactionRequest.getCategory().getName());
        Account account = accountService.findOneByCurrentUserAndCurrencyCode(CurrencyCode.valueOf(transactionRequest.getCurrencyCode()));
        return Transaction.builder()
                .amount(transactionRequest.getAmount())
                .category(category)
                .date(transactionRequest.getDate())
                .account(account)
                .paymentMethod(PaymentMethod.valueOf(transactionRequest.getPaymentMethod().toUpperCase()))
                .user(account.getUser())
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
                .user(userDetailsService.getCurrentUser())
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
                .currencyCode(CurrencyCode.valueOf(accountRequest.getCurrencyCode()))
                .user(userDetailsService.getCurrentUser())
                .build();
    }
}
