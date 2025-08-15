package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountService;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRepository;
import com.rainy.homebudgettracker.exchange.CurrencyConverter;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.handler.exception.*;
import com.rainy.homebudgettracker.images.S3Service;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.*;
import com.rainy.homebudgettracker.transaction.dto.TransactionRequest;
import com.rainy.homebudgettracker.transaction.dto.TransactionResponse;
import com.rainy.homebudgettracker.transaction.dto.TransactionUpdateRequest;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.service.queryfilter.TransactionFilter;
import com.rainy.homebudgettracker.transaction.service.queryfilter.TransactionSpecificationBuilder;
import com.rainy.homebudgettracker.transaction.repository.TransactionRepository;
import com.rainy.homebudgettracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static com.rainy.homebudgettracker.transaction.service.helper.BigDecimalNormalization.normalize;
import static com.rainy.homebudgettracker.transaction.service.queryfilter.TransactionSpecifications.byUserSub;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final S3Service s3Service;
    private final CategoryRepository categoryRepository;
    private final ExchangeService exchangeService;
    private final TransactionSpecificationBuilder transactionSpecificationBuilder;

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findCurrentUserTransactions(TransactionFilter filter, Pageable pageable) {
        String userSub = userService.getUserSub();
        Specification<Transaction> spec = transactionSpecificationBuilder.build(filter, userSub);

        return transactionRepository.findAll(spec, pageable).map(this::mapToTransactionResponse);
    }

    @Override
    @Transactional
    public TransactionResponse createTransactionForCurrentUser(UUID accountId, TransactionRequest transactionRequest) {
        Account account = accountService.findCurrentUserAccount(accountId);
        boolean needsConversion = transactionRequest.getCurrencyCode() != null
                && !transactionRequest.getCurrencyCode().equals(account.getCurrencyCode());

        if (needsConversion) {
            return createCurrencyConversionTransaction(account, transactionRequest);
        } else {
            return createStandardTransaction(account, transactionRequest);
        }
    }

    @Override
    @Transactional
    public TransactionResponse updateTransactionForCurrentUser(UUID transactionId, TransactionUpdateRequest request) {
        Transaction transaction = findAndVerifyTransactionOwner(transactionId);

        String userSub = userService.getUserSub();
        if (request.getCategoryName() != null) {
            Optional<Category> category = categoryRepository.findByUserSubAndName(userSub, request.getCategoryName());
            transaction.setCategory(category.orElseThrow(() -> new RecordDoesNotExistException("Category with name "
                    + request.getCategoryName() + " does not exist.")));
        }
        if (request.getTransactionMethod() != null) {
            transaction.setTransactionMethod(request.getTransactionMethod());
        }
        if (request.getDetails() != null) {
            transaction.setDetails(request.getDetails());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToTransactionResponse(savedTransaction);
    }

    @Override
    @Transactional
    public void deleteCurrentUserTransaction(UUID transactionId) {
        Transaction transaction = findAndVerifyTransactionOwner(transactionId);
        if (transaction.getImageFilePath() != null) {
            s3Service.deleteFile(transaction.getImageFilePath());
        }
        transactionRepository.delete(transaction);
    }

    @Override
    public byte[] generateCSVWithCurrentUserTransactions() {
        String userSub = userService.getUserSub();
        List<Transaction> transactions = transactionRepository.findAll(byUserSub(userSub));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {

            writer.append("sep=,\n");
            writer.append("Account name,Currency code,Amount,Category,Date,Transaction method,Description\n");

            for (Transaction transaction : transactions) {
                TransactionResponse dto = mapToTransactionResponse(transaction);
                String details = dto.details() == null ? "" : dto.details();
                writer.append(dto.account().getName())
                        .append(",").append(dto.account().getCurrencyCode())
                        .append(",").append(dto.amount())
                        .append(",").append(dto.category().getName())
                        .append(",").append(dto.date())
                        .append(",").append(dto.transactionMethod())
                        .append(",\"").append(details).append("\"\n");
            }
            writer.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new FileProcessingException("Error generating CSV file content", e);
        }
    }

    @Override
    @Transactional
    public TransactionResponse addImageToCurrentUserTransaction(UUID transactionId, MultipartFile file) {
        if (!userService.isPremiumUser()) {
            throw new PremiumStatusRequiredException("This feature is only available for premium users.");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image")) {
            throw new WrongFileTypeException("Invalid file type. Only images are allowed.");
        }

        Transaction transaction = findAndVerifyTransactionOwner(transactionId);
        String key = s3Service.uploadFile(file, userService.getUserSub(), transactionId);
        transaction.setImageFilePath(key);

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToTransactionResponse(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponse deleteImageFromCurrentUserTransaction(UUID transactionId) {
        Transaction transaction = findAndVerifyTransactionOwner(transactionId);
        if (transaction.getImageFilePath() != null) {
            s3Service.deleteFile(transaction.getImageFilePath());
            transaction.setImageFilePath(null);
            Transaction savedTransaction = transactionRepository.save(transaction);
            return mapToTransactionResponse(savedTransaction);
        }
        return mapToTransactionResponse(transaction);
    }

    private TransactionResponse createStandardTransaction(Account account, TransactionRequest request) {
        String userSub = userService.getUserSub();
        Category category = categoryRepository.findByUserSubAndName(userSub, request.getCategoryName().getName())
                .orElseThrow(() -> new RecordDoesNotExistException("Category with name " + request.getCategoryName()
                        + " does not exist."));

        Transaction transaction = modelMapper.map(request, Transaction.class, userSub, category, account);
        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToTransactionResponse(savedTransaction);
    }

    private TransactionResponse createCurrencyConversionTransaction(
            Account account, TransactionRequest originalRequest) {
        BigDecimal exchangeRate;
        String newDetails;
        CurrencyCode targetCurrency = account.getCurrencyCode();

        boolean isRateProvided = originalRequest.getExchangeRate() != null
                && originalRequest.getExchangeRate().compareTo(BigDecimal.ZERO) > 0;

        if (isRateProvided) {
            exchangeRate = normalize(originalRequest.getExchangeRate(), 4);
            newDetails = buildExchangeDetails(
                    originalRequest.getCurrencyCode().name(),
                    targetCurrency.name(),
                    exchangeRate.toString(),
                    false,
                    originalRequest.getDetails()
            );
        } else {
            exchangeRate = getCurrencyRate(originalRequest.getCurrencyCode(), targetCurrency);
            newDetails = buildExchangeDetails(
                    originalRequest.getCurrencyCode().name(),
                    targetCurrency.name(),
                    exchangeRate.toString(),
                    true,
                    originalRequest.getDetails()
            );
        }

        BigDecimal convertedAmount = CurrencyConverter.convert(
                originalRequest.getAmount(),
                exchangeRate,
                2);

        TransactionRequest convertedRequest = TransactionRequest.builder()
                .amount(convertedAmount)
                .date(originalRequest.getDate())
                .transactionMethod(originalRequest.getTransactionMethod())
                .details(newDetails)
                .categoryName(originalRequest.getCategoryName())
                .currencyCode(targetCurrency)
                .build();

        return createStandardTransaction(account, convertedRequest);
    }

    private String buildExchangeDetails(String from, String to, String rate, boolean withDate, String originalDetails) {
        String conversionInfo = from + "->" + to + ": " + rate + (withDate ? " - "
                + LocalDate.now(ZoneId.of("Europe/Warsaw")) : "");
        return (originalDetails == null || originalDetails.isEmpty())
                ? conversionInfo
                : conversionInfo + " | " + originalDetails;
    }

    private BigDecimal getCurrencyRate(CurrencyCode sourceCurrency, CurrencyCode targetCurrency) {
        ExchangeResponse exchangeResponse = exchangeService.getExchangeRate(sourceCurrency, targetCurrency);
        return new BigDecimal(exchangeResponse.conversionRate()).setScale(4, RoundingMode.HALF_UP);
    }

    private Transaction findAndVerifyTransactionOwner(UUID transactionId) {
        String userSub = userService.getUserSub();
        return transactionRepository.findById(transactionId)
                .map(transaction -> {
                    if (!transaction.getUserSub().equals(userSub)) {
                        throw new UserIsNotOwnerException("Transaction with id " + transactionId
                                + " does not belong to the user.");
                    }
                    return transaction;
                })
                .orElseThrow(() -> new RecordDoesNotExistException("Transaction with id "
                        + transactionId + " does not exist."));
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        if (transaction.getImageFilePath() == null) {
            return modelMapper.map(transaction, TransactionResponse.class);
        }
        return modelMapper.map(transaction, TransactionResponse.class, transaction.getImageFilePath());
    }
}
