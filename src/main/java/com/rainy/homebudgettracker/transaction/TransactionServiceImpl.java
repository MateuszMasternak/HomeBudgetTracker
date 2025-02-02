package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.account.AccountService;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.exchange.CurrencyConverter;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.handler.exception.*;
import com.rainy.homebudgettracker.images.ImageService;
import com.rainy.homebudgettracker.images.S3Service;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
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
import java.util.*;

import static com.rainy.homebudgettracker.transaction.BigDecimalNormalization.normalize;

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
    private final ImageService imageService;

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        String imageUrl = imageService.getImageUrl(transaction);
        return imageUrl == null
                ? modelMapper.map(transaction, TransactionResponse.class)
                : modelMapper.map(transaction, TransactionResponse.class, imageUrl);
    }

    @Override
    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(UUID accountId, Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        Page<Transaction> transactions = transactionRepository.findAllByAccount(account, pageable);
        return transactions.map(this::mapToTransactionResponse);
    }

    @Override
    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId, CategoryRequest categoryName, Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Category category = categoryService.findCurrentUserCategory(categoryName.getName());
        Account account = accountService.findCurrentUserAccount(accountId);

        Page<Transaction> transactions = transactionRepository.findAllByAccountAndCategory(
                account, category, pageable
        );
        return transactions.map(this::mapToTransactionResponse);
    }

    @Override
    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId,
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
        return transactions.map(this::mapToTransactionResponse);
    }

    @Override
    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId,
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
        return transactions.map(this::mapToTransactionResponse);
    }

    @Transactional
    @Override
    public TransactionResponse createTransactionForCurrentUser(UUID accountId, TransactionRequest transactionRequest)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        Account account = accountService.findCurrentUserAccount(accountId);
        return saveTransactionForCurrentUser(account, transactionRequest);
    }

    @Transactional
    @Override
    public TransactionResponse createTransactionForCurrentUser(
            UUID accountId,
            BigDecimal exchangeRate,
            TransactionRequest transactionRequest)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        CurrencyCode targetCurrency = account.getCurrencyCode();
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


        return saveTransactionForCurrentUser(account, convertedTransactionRequest);
    }

    private TransactionResponse saveTransactionForCurrentUser(Account account, TransactionRequest transactionRequest)
            throws RecordDoesNotExistException {

        Category category = categoryService.findCurrentUserCategory(
                transactionRequest.getCategoryName().getName());
        String userSub = userService.getUserSub();

        Transaction transaction = modelMapper.map(transactionRequest, Transaction.class, userSub, category, account);
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
                .transactionMethod(transactionRequest.getTransactionMethod())
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

    @Transactional
    @Override
    public void deleteCurrentUserTransaction(UUID transactionId) throws
            RecordDoesNotExistException,
            UserIsNotOwnerException {

        String userSub = userService.getUserSub();
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
        if (transaction.isEmpty()) {
            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
        } else if (!transaction.get().getUserSub().equals(userSub)) {
            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to user.");
        } else {
            transactionRepository.deleteById(transactionId);
        }
    }

    @Override
    public SumResponse sumCurrentUserPositiveAmount(UUID accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumPositiveAmountByAccount(account), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserNegativeAmount(UUID accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumNegativeAmountByAccount(account), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserAmount(UUID accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumAmountByAccount(account), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserPositiveAmount(UUID accountId, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumPositiveAmountByAccountAndDateBetween(
                account, startDate, endDate), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserPositiveAmount(UUID accountId, CategoryRequest categoryName, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        Category category = categoryService.findCurrentUserCategory(categoryName.getName());
        BigDecimal sum = normalize(transactionRepository.sumPositiveAmountByAccountAndCategoryAndDateBetween(
                account, category, startDate, endDate), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));
        response.setCategory(modelMapper.map(category, CategoryResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserNegativeAmount(UUID accountId, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumNegativeAmountByAccountAndDateBetween(
                account, startDate, endDate), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserNegativeAmount(UUID accountId, CategoryRequest categoryName, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        Category category = categoryService.findCurrentUserCategory(categoryName.getName());
        BigDecimal sum = normalize(transactionRepository.sumNegativeAmountByAccountAndCategoryAndDateBetween(
                account, category, startDate, endDate), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));
        response.setCategory(modelMapper.map(category, CategoryResponse.class));

        return response;
    }

    @Override
    public List<TransactionResponse> findCurrentUserTransactionsAsResponses() {
        String userSub = userService.getUserSub();
        Iterable<Transaction> transactions = transactionRepository.findAllByUserSub(userSub);
        List<TransactionResponse> transactionResponses = new ArrayList<>();
        transactions.forEach(t -> transactionResponses.add(modelMapper.map(t, TransactionResponse.class)));
        return transactionResponses;
    }

    @Override
    public byte[] generateCSVWithCurrentUserTransactions() throws IOException {
        String userSub = userService.getUserSub();
        List<TransactionResponse> transactionResponses = findCurrentUserTransactionsAsResponses();

        Path csvFilePath = Paths.get("temp_transactions_" + userSub + "_" + LocalDate.now() + ".csv");

        try (FileWriter writer = new FileWriter(csvFilePath.toString())) {
            writer.append("sep=,\n"); // separator for microsoft excel
            writer.append("Account name,Currency code,Amount,Category,Date,Transaction method,Description\n");
            for (TransactionResponse transactionResponse : transactionResponses) {
                String details = transactionResponse.getDetails() == null ? "" : transactionResponse.getDetails();
                writer.append(transactionResponse.getAccount().getName())
                        .append(",")
                        .append(transactionResponse.getAccount().getCurrencyCode())
                        .append(",")
                        .append(transactionResponse.getAmount())
                        .append(",")
                        .append(transactionResponse.getCategory().getName())
                        .append(",")
                        .append(transactionResponse.getDate())
                        .append(",")
                        .append(transactionResponse.getTransactionMethod())
                        .append(",")
                        .append(details)
                        .append("\n");
            }
        }

        byte[] fileContent = Files.readAllBytes(csvFilePath);
        Files.delete(csvFilePath);

        return fileContent;
    }

    @Override
    public TransactionResponse addImageToCurrentUserTransaction(UUID transactionId, MultipartFile file)
            throws RecordDoesNotExistException,
            UserIsNotOwnerException,
            ImageUploadException,
            WrongFileTypeException, PremiumStatusRequiredException {

        if (!userService.isPremiumUser()) {
            throw new PremiumStatusRequiredException("This feature is only available for premium users.");
        }

        String userSub = userService.getUserSub();
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);

        if (transaction.isEmpty()) {
            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
        } else if (!transaction.get().getUserSub().equals(userSub)) {
            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to the user.");
        } else if (file.getContentType() == null || !file.getContentType().startsWith("image")) {
            throw new WrongFileTypeException("Invalid file type. Only images are allowed.");
        }

        String key = s3Service.uploadFile(file, userSub, transactionId);

        transaction.get().setImageFilePath(key);

        transactionRepository.save(transaction.get());
        String imageUrl = imageService.getImageUrl(transaction.get());
        return modelMapper.map(transaction.get(), TransactionResponse.class, imageUrl);
    }

    @Override
    public TransactionResponse deleteImageFromCurrentUserTransaction(UUID transactionId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        String userSub = userService.getUserSub();
        Optional<Transaction> transaction = transactionRepository.findById(transactionId);

        if (transaction.isEmpty()) {
            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
        } else if (!transaction.get().getUserSub().equals(userSub)) {
            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to the user.");
        }

        String key = transaction.get().getImageFilePath();
        s3Service.deleteFile(key);

        transaction.get().setImageFilePath(null);

        transactionRepository.save(transaction.get());
        return modelMapper.map(transaction.get(), TransactionResponse.class);
    }
}
