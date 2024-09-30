package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountService;
import com.rainy.homebudgettracker.auth.UserDetailsServiceImpl;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final ExchangeService exchangeService;
    private final ModelMapper modelMapper;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Page<TransactionResponse> findAllByCurrentUserAndAccount(CurrencyCode currencyCode, Pageable pageable)
            throws RecordDoesNotExistException {
        User user = userDetailsService.getCurrentUser();
        Account account = accountService.findOneByCurrentUserAndCurrencyCode(currencyCode);
        Page<Transaction> transactions = transactionRepository.findAllByUserAndAccount(user, account, pageable);
        return transactions.map(t -> modelMapper.map(t, TransactionResponse.class));
    }

    @Override
    public Page<TransactionResponse> findAllByCurrentUserAndAccountAndCategory(
            CurrencyCode currencyCode, CategoryRequest categoryName, Pageable pageable
    ) throws RecordDoesNotExistException {
        User user = userDetailsService.getCurrentUser();
        Category category = categoryService.findOneByCurrentUserAndName(categoryName.getName());

        Account account = accountService.findOneByCurrentUserAndCurrencyCode(currencyCode);

        Page<Transaction> transactions = transactionRepository.findAllByUserAndAccountAndCategory(
                user, account, category, pageable
        );
        return transactions.map(t -> modelMapper.map(t, TransactionResponse.class));
    }

    @Override
    public Page<TransactionResponse> findAllByCurrentUserAndAccountAndDateBetween(
            CurrencyCode currencyCode,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) throws RecordDoesNotExistException {
        User user = userDetailsService.getCurrentUser();
        Account account = accountService.findOneByCurrentUserAndCurrencyCode(currencyCode);
        Page<Transaction> transactions = transactionRepository.findAllByUserAndAccountAndDateBetween(
                user,
                account,
                startDate,
                endDate,
                pageable
        );
        return transactions.map(t -> modelMapper.map(t, TransactionResponse.class));
    }

    @Override
    public Page<TransactionResponse> findAllByCurrentUserAndAccountAndCategoryAndDateBetween(
            CurrencyCode currencyCode,
            String categoryName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) throws RecordDoesNotExistException {
        User user = userDetailsService.getCurrentUser();
        Category category = categoryService.findOneByCurrentUserAndName(categoryName);
        Account account = accountService.findOneByCurrentUserAndCurrencyCode(currencyCode);

        Page<Transaction> transactions = transactionRepository.findAllByUserAndAccountAndCategoryAndDateBetween(
                user,
                account,
                category,
                startDate,
                endDate,
                pageable
        );
        return transactions.map(t -> modelMapper.map(t, TransactionResponse.class));
    }

    @Transactional
    @Override
    public TransactionResponse createTransactionForCurrentUser(TransactionRequest transactionRequest)
            throws RecordDoesNotExistException {
        return saveTransactionForCurrentUser(transactionRequest);
    }

    @Transactional
    @Override
    public TransactionResponse createTransactionForCurrentUser(CurrencyCode targetCurrency, BigDecimal exchangeRate,
                                                               TransactionRequest transactionRequest
    ) throws RecordDoesNotExistException {
        if (exchangeRate != null) {
            convertCurrency(transactionRequest, exchangeRate, targetCurrency);
        } else {
            ExchangeResponse exchangeResponse = exchangeService.getExchangeRate(
                    transactionRequest.getCurrencyCode(),
                    targetCurrency
            );
            String apiExchangeRate = exchangeResponse.getConversionRate();
            BigDecimal apiExchangeRateBG = new BigDecimal(apiExchangeRate);
            convertCurrency(transactionRequest, apiExchangeRateBG, targetCurrency);
        }

        return saveTransactionForCurrentUser(transactionRequest);
    }

    @Override
    public void deleteCurrentUserTransaction(Long transactionId) throws
            RecordDoesNotExistException,
            UserIsNotOwnerException {
        User user = userDetailsService.getCurrentUser();
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isEmpty()) {
            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
        } else if (!transaction.get().getUser().getEmail().equals(user.getEmail())) {
            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to user.");
        } else {
            transactionRepository.deleteById(transactionId);
        }
    }

    @Override
    public SumResponse sumPositiveAmountByCurrentUserAndAccount(CurrencyCode currencyCode)
            throws RecordDoesNotExistException {
        User user = userDetailsService.getCurrentUser();
        Account account = accountService.findOneByCurrentUserAndCurrencyCode(currencyCode);
        BigDecimal sum = transactionRepository.sumPositiveAmountByUserAndAccount(user, account);
        String amount = sum == null ? "0" : sum.toString();
        return SumResponse.builder().amount(amount).build();
    }

    @Override
    public SumResponse sumNegativeAmountByCurrentUserAndAccount(CurrencyCode currencyCode)
            throws RecordDoesNotExistException {
        User user = userDetailsService.getCurrentUser();
        Account account = accountService.findOneByCurrentUserAndCurrencyCode(currencyCode);
        BigDecimal sum = transactionRepository.sumNegativeAmountByUserAndAccount(user, account);
        String amount = sum == null ? "0" : sum.toString();
        return SumResponse.builder().amount(amount).build();
    }

    @Override
    public SumResponse sumAmountByCurrentUserAndAccount(CurrencyCode currencyCode) throws RecordDoesNotExistException {
        User user = userDetailsService.getCurrentUser();
        Account account = accountService.findOneByCurrentUserAndCurrencyCode(currencyCode);
        BigDecimal sum = transactionRepository.sumAmountByUserAndAccount(user, account);
        String amount = sum == null ? "0" : sum.toString();
        return SumResponse.builder().amount(amount).build();
    }

    @Override
    public List<TransactionResponse> findAllByCurrentUser() {
        User user = userDetailsService.getCurrentUser();
        Iterable<Transaction> transactions = transactionRepository.findAllByUser(user);
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        transactions.forEach(t -> transactionResponses.add(modelMapper.map(t, TransactionResponse.class)));
        return transactionResponses;
    }

    @Override
    public byte[] generateCsvFileForCurrentUserTransactions() throws IOException {
        User user = userDetailsService.getCurrentUser();
        List<TransactionResponse> transactionResponses = findAllByCurrentUser();

        Path csvFilePath = Paths.get("temp_transactions_" + user.getId() + "_" + LocalDate.now() + ".csv");

        try (FileWriter writer = new FileWriter(csvFilePath.toString())) {
            writer.append("sep=,\n"); // separator for microsoft excel
            writer.append("ID,Amount,Category,Date,Currency code,Payment method\n");
            for (TransactionResponse transactionResponse : transactionResponses) {
                System.out.println(transactionResponse);
                writer.append(transactionResponse.getId().toString())
                        .append(",")
                        .append(transactionResponse.getAmount())
                        .append(",")
                        .append(transactionResponse.getCategory().getName())
                        .append(",")
                        .append(transactionResponse.getDate())
                        .append(",")
                        .append(transactionResponse.getAccount().getCurrencyCode())
                        .append(",")
                        .append(transactionResponse.getPaymentMethod())
                        .append("\n");
            }
        }

        byte[] fileContent = Files.readAllBytes(csvFilePath);
        Files.delete(csvFilePath);

        return fileContent;
    }

    private TransactionResponse saveTransactionForCurrentUser(TransactionRequest transactionRequest)
            throws RecordDoesNotExistException {
        Category category = categoryService.findOneByCurrentUserAndName(
                transactionRequest.getCategory().getName());
        Account account = accountService.findOneByCurrentUserAndCurrencyCode(transactionRequest.getCurrencyCode());

        Transaction transaction = modelMapper.mapTransactionRequestToTransaction(transactionRequest, account, category);

        transaction = transactionRepository.save(transaction);

        return modelMapper.map(transaction, TransactionResponse.class);
    }

    private void convertCurrency(TransactionRequest transaction, BigDecimal rate, CurrencyCode targetCurrency) {
        transaction.setAmount(
                transaction.getAmount()
                        .multiply(rate));
        transaction.setCurrencyCode(targetCurrency);
    }
}
