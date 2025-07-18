//package com.rainy.homebudgettracker.transaction.service;
//
//import com.rainy.homebudgettracker.account.Account;
//import com.rainy.homebudgettracker.account.AccountService;
//import com.rainy.homebudgettracker.category.Category;
//import com.rainy.homebudgettracker.category.CategoryRepository;
//import com.rainy.homebudgettracker.category.CategoryRequest;
//import com.rainy.homebudgettracker.category.CategoryService;
//import com.rainy.homebudgettracker.exchange.CurrencyConverter;
//import com.rainy.homebudgettracker.exchange.ExchangeResponse;
//import com.rainy.homebudgettracker.exchange.ExchangeService;
//import com.rainy.homebudgettracker.handler.exception.*;
//import com.rainy.homebudgettracker.images.ImageService;
//import com.rainy.homebudgettracker.images.S3Service;
//import com.rainy.homebudgettracker.mapper.ModelMapper;
//import com.rainy.homebudgettracker.transaction.*;
//import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
//import com.rainy.homebudgettracker.user.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.*;
//
//import static com.rainy.homebudgettracker.transaction.BigDecimalNormalization.normalize;
//
//@Service
//@RequiredArgsConstructor
//public class TransactionServiceImpl implements TransactionService {
//    private final TransactionRepository transactionRepository;
//    private final CategoryRepository categoryService;
//    private final AccountService accountService;
//    private final ExchangeService exchangeService;
//    private final ModelMapper modelMapper;
//    private final UserService userService;
//    private final S3Service s3Service;
//    private final ImageService imageService;
//
//    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
//        String imageUrl = imageService.getImageUrl(transaction);
//        return imageUrl == null
//                ? modelMapper.map(transaction, TransactionResponse.class)
//                : modelMapper.map(transaction, TransactionResponse.class, imageUrl);
//    }
//
//    @Override
//    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(UUID accountId, Pageable pageable) {
//        Account account = accountService.findCurrentUserAccount(accountId);
//        Page<Transaction> transactions = transactionRepository.findAllByAccount(account, pageable);
//        return transactions.map(this::mapToTransactionResponse);
//    }
//
//    @Override
//    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
//            UUID accountId, CategoryRequest categoryName, Pageable pageable)
//            throws RecordDoesNotExistException, UserIsNotOwnerException {
//
//        Category category = categoryService.findCurrentUserCategory(categoryName.getName());
//        Account account = accountService.findCurrentUserAccount(accountId);
//
//        Page<Transaction> transactions = transactionRepository.findAllByAccountAndCategory(
//                account, category, pageable
//        );
//        return transactions.map(this::mapToTransactionResponse);
//    }
//
//    @Override
//    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
//            UUID accountId,
//            LocalDate startDate,
//            LocalDate endDate,
//            Pageable pageable)
//            throws RecordDoesNotExistException, UserIsNotOwnerException {
//
//        Account account = accountService.findCurrentUserAccount(accountId);
//        Page<Transaction> transactions = transactionRepository.findAllByAccountAndDateBetween(
//                account,
//                startDate,
//                endDate,
//                pageable
//        );
//        return transactions.map(this::mapToTransactionResponse);
//    }
//
//    @Override
//    public Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
//            UUID accountId,
//            CategoryRequest categoryName,
//            LocalDate startDate,
//            LocalDate endDate,
//            Pageable pageable)
//            throws RecordDoesNotExistException, UserIsNotOwnerException {
//
//        Category category = categoryService.findCurrentUserCategory(categoryName.getName());
//        Account account = accountService.findCurrentUserAccount(accountId);
//
//        Page<Transaction> transactions = transactionRepository.findAllByAccountAndCategoryAndDateBetween(
//                account,
//                category,
//                startDate,
//                endDate,
//                pageable
//        );
//        return transactions.map(this::mapToTransactionResponse);
//    }
//
//    @Transactional
//    @Override
//    public TransactionResponse createTransactionForCurrentUser(UUID accountId, TransactionRequest transactionRequest)
//            throws RecordDoesNotExistException, UserIsNotOwnerException {
//        Account account = accountService.findCurrentUserAccount(accountId);
//        return saveTransactionForCurrentUser(account, transactionRequest);
//    }
//
//    @Transactional
//    @Override
//    public TransactionResponse createTransactionForCurrentUser(
//            UUID accountId,
//            BigDecimal exchangeRate,
//            TransactionRequest transactionRequest)
//            throws RecordDoesNotExistException, UserIsNotOwnerException {
//
//        Account account = accountService.findCurrentUserAccount(accountId);
//        CurrencyCode targetCurrency = account.getCurrencyCode();
//
//        if (exchangeRate == null) {
//            exchangeRate = getCurrencyRate(
//                    transactionRequest.getCurrencyCode(),
//                    targetCurrency);
//
//            addExchangeDetails(
//                    transactionRequest,
//                    targetCurrency.name(),
//                    exchangeRate.toString(),
//                    true);
//        } else {
//            addExchangeDetails(
//                    transactionRequest,
//                    targetCurrency.name(),
//                    exchangeRate.toString(),
//                    false);
//        }
//
//        BigDecimal convertedAmount = CurrencyConverter.convert(
//                transactionRequest.getAmount(),
//                normalize(exchangeRate, 2),
//                2);
//
//        transactionRequest.setAmount(convertedAmount);
//        transactionRequest.setCurrencyCode(targetCurrency);
//
//        return saveTransactionForCurrentUser(account, transactionRequest);
//    }
//
//    private TransactionResponse saveTransactionForCurrentUser(Account account, TransactionRequest transactionRequest)
//            throws RecordDoesNotExistException {
//
//        Category category = categoryService.findCurrentUserCategory(
//                transactionRequest.getCategoryName().getName());
//        String userSub = userService.getUserSub();
//
//        Transaction transaction = modelMapper.map(transactionRequest, Transaction.class, userSub, category, account);
//        transaction = transactionRepository.save(transaction);
//        return modelMapper.map(transactionRepository.save(transaction), TransactionResponse.class);
//    }
//
//    private BigDecimal getCurrencyRate(CurrencyCode sourceCurrency, CurrencyCode targetCurrency) {
//        ExchangeResponse exchangeResponse = exchangeService.getExchangeRate(sourceCurrency, targetCurrency);
//        return new BigDecimal(exchangeResponse.getConversionRate()).setScale(2, RoundingMode.HALF_UP);
//    }
//
//    private void addExchangeDetails(
//            TransactionRequest transactionRequest, String targetCurrency, String apiExchangeRate, boolean date) {
//        if (transactionRequest.getDetails() == null) {
//            transactionRequest.setDetails(
//                    transactionRequest.getCurrencyCode() + "->" + targetCurrency + ": " + apiExchangeRate
//                            + (date ? " - " + LocalDate.now(ZoneId.of("Europe/Warsaw")) : ""));
//        } else {
//            transactionRequest.setDetails(
//                    transactionRequest.getCurrencyCode() + "->" + targetCurrency + ": " + apiExchangeRate
//                            + (date ? " - " + LocalDate.now(ZoneId.of("Europe/Warsaw")) : "")
//                            + " | " + transactionRequest.getDetails());
//        }
//    }
//
//    @Transactional
//    @Override
//    public void deleteCurrentUserTransaction(UUID transactionId) throws
//            RecordDoesNotExistException,
//            UserIsNotOwnerException {
//
//        String userSub = userService.getUserSub();
//        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
//        if (transaction.isEmpty()) {
//            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
//        } else if (!transaction.get().getUserSub().equals(userSub)) {
//            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to user.");
//        } else {
//            transactionRepository.deleteById(transactionId);
//        }
//    }
//
//    @Override
//    public List<TransactionResponse> findCurrentUserTransactionsAsResponses() {
//        String userSub = userService.getUserSub();
//        Iterable<Transaction> transactions = transactionRepository.findAllByUserSub(userSub);
//        List<TransactionResponse> transactionResponses = new ArrayList<>();
//        transactions.forEach(t -> transactionResponses.add(modelMapper.map(t, TransactionResponse.class)));
//        return transactionResponses;
//    }
//
//    @Override
//    public byte[] generateCSVWithCurrentUserTransactions() {
//        List<TransactionResponse> transactionResponses = findCurrentUserTransactionsAsResponses();
//        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
//             OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
//
//            writer.append("sep=,\n"); // Microsoft Excel compatibility
//            writer.append("Account name,Currency code,Amount,Category,Date,Transaction method,Description\n");
//
//            for (TransactionResponse transactionResponse : transactionResponses) {
//                String details = transactionResponse.getDetails() == null ? "" : transactionResponse.getDetails();
//                writer.append(transactionResponse.getAccount().getName())
//                        .append(",")
//                        .append(transactionResponse.getAccount().getCurrencyCode())
//                        .append(",")
//                        .append(transactionResponse.getAmount())
//                        .append(",")
//                        .append(transactionResponse.getCategory().getName())
//                        .append(",")
//                        .append(transactionResponse.getDate())
//                        .append(",")
//                        .append(transactionResponse.getTransactionMethod())
//                        .append(",")
//                        .append(details)
//                        .append("\n");
//            }
//
//            writer.flush();
//            return baos.toByteArray();
//
//        } catch (IOException e) {
//            throw new FileProcessingException("Error generating CSV file content in memory", e);
//        }
//    }
//
//    @Override
//    public TransactionResponse addImageToCurrentUserTransaction(UUID transactionId, MultipartFile file) {
//        if (!userService.isPremiumUser()) {
//            throw new PremiumStatusRequiredException("This feature is only available for premium users.");
//        }
//
//        Transaction transaction = getTransactionByTransactionId(transactionId);
//
//        if (file.getContentType() == null || !file.getContentType().startsWith("image")) {
//            throw new WrongFileTypeException("Invalid file type. Only images are allowed.");
//        }
//
//        String key = s3Service.uploadFile(file, userService.getUserSub(), transactionId);
//
//        transaction.setImageFilePath(key);
//
//        transactionRepository.save(transaction);
//        String imageUrl = imageService.getImageUrl(transaction);
//        return modelMapper.map(transaction, TransactionResponse.class, imageUrl);
//    }
//
//    @Override
//    public TransactionResponse deleteImageFromCurrentUserTransaction(UUID transactionId) {
//        Transaction transaction = getTransactionByTransactionId(transactionId);
//
//        String key = transaction.getImageFilePath();
//        s3Service.deleteFile(key);
//
//        transaction.setImageFilePath(null);
//
//        transactionRepository.save(transaction);
//        return modelMapper.map(transaction, TransactionResponse.class);
//    }
//
//    @Override
//    public TransactionResponse updateTransactionForCurrentUser(UUID transactionId, TransactionUpdateRequest request) {
//        Transaction transaction = getTransactionByTransactionId(transactionId);
//
//        if (request.getCategoryName() != null) {
//            Category category = categoryService.findCurrentUserCategory(
//                    request.getCategoryName().getName());
//            transaction.setCategory(category);
//        }
//
//        if (request.getTransactionMethod() != null) {
//            transaction.setTransactionMethod(request.getTransactionMethod());
//        }
//
//        if (request.getDetails() != null) {
//            transaction.setDetails(request.getDetails());
//        }
//
//        return modelMapper.map(transactionRepository.save(transaction), TransactionResponse.class);
//    }
//
//    private Transaction getTransactionByTransactionId(UUID transactionId) {
//        String userSub = userService.getUserSub();
//        Optional<Transaction> transaction = transactionRepository.findById(transactionId);
//
//        if (transaction.isEmpty()) {
//            throw new RecordDoesNotExistException("Transaction with id " + transactionId + " does not exist.");
//        } else if (!transaction.get().getUserSub().equals(userSub)) {
//            throw new UserIsNotOwnerException("Transaction with id " + transactionId + " does not belong to the user.");
//        }
//
//        return transaction.get();
//    }
//}
