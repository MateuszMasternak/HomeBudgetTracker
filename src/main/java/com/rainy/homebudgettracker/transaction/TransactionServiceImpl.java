package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.account.AccountService;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.exchange.CurrencyConverter;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.handler.exception.ImageUploadException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.handler.exception.WrongFileTypeException;
import com.rainy.homebudgettracker.images.S3Service;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import com.rainy.homebudgettracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final ExchangeService exchangeService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final S3Service s3Service;

    @Override
    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(Long accountId, Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        Page<Transaction> transactions = transactionRepository.findAllByAccount(account, pageable);
        return transactions.map(t -> modelMapper.map(t, TransactionResponse.class));
    }

    @Override
    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            Long accountId, CategoryRequest categoryName, Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Category category = categoryService.findCurrentUserCategory(categoryName.getName());
        Account account = accountService.findCurrentUserAccount(accountId);

        Page<Transaction> transactions = transactionRepository.findAllByAccountAndCategory(
                account, category, pageable
        );
        return transactions.map(t -> modelMapper.map(t, TransactionResponse.class));
    }

    @Override
    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            Long accountId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        Page<Transaction> transactions = transactionRepository.findAllByAccountAndDateBetween(
                account,
                startDate,
                endDate,
                pageable
        );
        return transactions.map(t -> modelMapper.map(t, TransactionResponse.class));
    }

    @Override
    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            Long accountId,
            CategoryRequest categoryName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Category category = categoryService.findCurrentUserCategory(categoryName.getName());
        Account account = accountService.findCurrentUserAccount(accountId);

        Page<Transaction> transactions = transactionRepository.findAllByAccountAndCategoryAndDateBetween(
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
    public TransactionResponse createTransactionForCurrentUser(Long accountId, TransactionRequest transactionRequest)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        return saveTransactionForCurrentUser(accountId, transactionRequest);
    }

    @Transactional
    @Override
    public TransactionResponse createTransactionForCurrentUser(
            Long accountId,
            CurrencyCode targetCurrency,
            BigDecimal exchangeRate,
            TransactionRequest transactionRequest)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        TransactionRequest convertedTransactionRequest = getTransactionRequestWithUpdatedCurrency(
                transactionRequest,
                CurrencyConverter.convert(
                        transactionRequest.getAmount(),
                        Objects.requireNonNullElseGet(
                                exchangeRate,
                                () -> {
                                    BigDecimal currencyRate = getCurrencyRate(
                                            transactionRequest.getCurrencyCode(),
                                            targetCurrency);
                                    addExchangeDetails(
                                            transactionRequest,
                                            targetCurrency.name(),
                                            currencyRate.toString());
                                    return currencyRate;
                                }).setScale(2, RoundingMode.HALF_UP),
                        2),
                targetCurrency);


        return saveTransactionForCurrentUser(accountId, convertedTransactionRequest);
    }

    private TransactionResponse saveTransactionForCurrentUser(Long accountId, TransactionRequest transactionRequest)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Category category = categoryService.findCurrentUserCategory(
                transactionRequest.getCategoryName().getName());
        Account account = accountService.findCurrentUserAccount(accountId);

        Transaction transaction = modelMapper.mapTransactionRequestToTransaction(transactionRequest, account, category);
        transaction = transactionRepository.save(transaction);
        return modelMapper.map(transactionRepository.save(transaction), TransactionResponse.class);
    }

    private TransactionRequest getTransactionRequestWithUpdatedCurrency(
            TransactionRequest transactionRequest, BigDecimal newValue, CurrencyCode targetCurrency) {

        return TransactionRequest.builder()
                .amount(newValue)
                .categoryName(transactionRequest.getCategoryName())
                .date(transactionRequest.getDate())
                .currencyCode(targetCurrency)
                .paymentMethod(transactionRequest.getPaymentMethod())
                .details(transactionRequest.getDetails())
                .build();
    }

    private BigDecimal getCurrencyRate(CurrencyCode sourceCurrency, CurrencyCode targetCurrency) {
        ExchangeResponse exchangeResponse = exchangeService.getExchangeRate(sourceCurrency, targetCurrency);
        return new BigDecimal(exchangeResponse.getConversionRate()).setScale(2, RoundingMode.HALF_UP);
    }

    private void addExchangeDetails(
            TransactionRequest transactionRequest, String targetCurrency, String apiExchangeRate) {
        transactionRequest.setDetails(
                transactionRequest.getCurrencyCode() + "->" + targetCurrency + ": " + apiExchangeRate);
    }

    @Override
    public void deleteCurrentUserTransaction(Long transactionId) throws
            RecordDoesNotExistException,
            UserIsNotOwnerException {

        User user = userService.getCurrentUser();
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
    public SumResponse sumCurrentUserPositiveAmount(Long accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = transactionRepository.sumPositiveAmountByAccount(account).setScale(
                2, RoundingMode.HALF_UP);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserNegativeAmount(Long accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = transactionRepository.sumNegativeAmountByAccount(account).setScale(
                2, RoundingMode.HALF_UP);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserAmount(Long accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = transactionRepository.sumAmountByAccount(account).setScale(
                2, RoundingMode.HALF_UP);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public List<TransactionResponse> findCurrentUserTransactionsAsResponses() {
        User user = userService.getCurrentUser();
        Iterable<Transaction> transactions = transactionRepository.findAllByUser(user);
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        transactions.forEach(t -> transactionResponses.add(modelMapper.map(t, TransactionResponse.class)));
        return transactionResponses;
    }

    @Override
    public byte[] generateCSVWithCurrentUserTransactions() throws IOException {
        User user = userService.getCurrentUser();
        List<TransactionResponse> transactionResponses = findCurrentUserTransactionsAsResponses();

        Path csvFilePath = Paths.get("temp_transactions_" + user.getId() + "_" + LocalDate.now() + ".csv");

        try (FileWriter writer = new FileWriter(csvFilePath.toString())) {
            writer.append("sep=,\n"); // separator for microsoft excel
            writer.append("ID,Amount,Category,Date,Currency code,Payment method\n");
            for (TransactionResponse transactionResponse : transactionResponses) {
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

    @Override
    public TransactionResponse addImageToCurrentUserTransaction(Long transactionId, MultipartFile file)
            throws RecordDoesNotExistException,
            UserIsNotOwnerException,
            ImageUploadException,
            WrongFileTypeException {

        User user = userService.getCurrentUser();
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);

        if (transaction.isEmpty()) {
            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
        } else if (!transaction.get().getUser().getEmail().equals(user.getEmail())) {
            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to the user.");
        } else if (file.getContentType() == null || !file.getContentType().startsWith("image")) {
            throw new WrongFileTypeException("Invalid file type. Only images are allowed.");
        }

        String key = s3Service.uploadFile(file, user.getId(), transactionId);

        transaction.get().setImageFilePath(key);

        transactionRepository.save(transaction.get());
        return modelMapper.map(transaction.get(), TransactionResponse.class);
    }

    @Override
    public TransactionResponse deleteImageFromCurrentUserTransaction(Long transactionId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        User user = userService.getCurrentUser();
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);

        if (transaction.isEmpty()) {
            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
        } else if (!transaction.get().getUser().getEmail().equals(user.getEmail())) {
            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to the user.");
        }

        String key = transaction.get().getImageFilePath();
        s3Service.deleteFile(key);

        transaction.get().setImageFilePath(null);

        transactionRepository.save(transaction.get());
        return modelMapper.map(transaction.get(), TransactionResponse.class);
    }
}
