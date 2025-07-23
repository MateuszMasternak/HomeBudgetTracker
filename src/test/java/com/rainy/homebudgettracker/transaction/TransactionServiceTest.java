package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountService;
import com.rainy.homebudgettracker.category.*;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.handler.exception.PremiumStatusRequiredException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.handler.exception.WrongFileTypeException;
import com.rainy.homebudgettracker.images.ImageService;
import com.rainy.homebudgettracker.images.S3Service;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.repository.TransactionRepository;
import com.rainy.homebudgettracker.transaction.service.TransactionServiceImpl;
import com.rainy.homebudgettracker.transaction.service.queryfilter.TransactionFilter;
import com.rainy.homebudgettracker.transaction.service.queryfilter.TransactionSpecificationBuilder;
import com.rainy.homebudgettracker.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserService userService;
    @Mock
    private ExchangeService exchangeService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private TransactionSpecificationBuilder transactionSpecificationBuilder;
    @Mock
    private S3Service s3Service;
    @Mock
    private ImageService imageService;

    @Nested
    @DisplayName("Finding Transactions")
    class FindingTransactions {

        @Test
        @DisplayName("should find transactions using filter and pageable")
        void findCurrentUserTransactions_shouldUseFilter() {
            TransactionFilter filter = new TransactionFilter(TestData.ACCOUNT.getId(), null, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> transactionPage = new PageImpl<>(List.of(TestData.TRANSACTION));
            Specification<Transaction> dummySpecification = Specification.where(null);

            when(userService.getUserSub()).thenReturn(TestData.USER_SUB);

            when(transactionSpecificationBuilder.build(any(TransactionFilter.class), anyString()))
                    .thenReturn(dummySpecification);

            when(transactionRepository.findAll(dummySpecification, pageable)).thenReturn(transactionPage);

            transactionService.findCurrentUserTransactions(filter, pageable);

            verify(transactionRepository).findAll(dummySpecification, pageable);
        }

        @Nested
        @DisplayName("Creating Transactions")
        class CreatingTransactions {

            @Test
            @DisplayName("should create a standard transaction and return a valid response")
            void createTransactionForCurrentUser_shouldCreateStandardTransaction() {
                when(userService.getUserSub()).thenReturn(TestData.USER_SUB);
                when(accountService.findCurrentUserAccount(TestData.ACCOUNT.getId())).thenReturn(TestData.ACCOUNT);
                when(categoryRepository.findByUserSubAndName(
                        TestData.USER_SUB, TestData.CATEGORY.getName()
                )).thenReturn(Optional.of(TestData.CATEGORY));

                when(modelMapper.map(TestData.TRANSACTION_REQUEST, Transaction.class, TestData.USER_SUB,
                        TestData.CATEGORY, TestData.ACCOUNT)).thenReturn(TestData.TRANSACTION);

                when(transactionRepository.save(any(Transaction.class))).thenReturn(TestData.TRANSACTION);

                when(modelMapper.map(TestData.TRANSACTION, TransactionResponse.class))
                        .thenReturn(TestData.TRANSACTION_RESPONSE);

                TransactionResponse actualResponse = transactionService.createTransactionForCurrentUser(
                        TestData.ACCOUNT.getId(), TestData.TRANSACTION_REQUEST
                );

                assertThat(actualResponse).isEqualTo(TestData.TRANSACTION_RESPONSE);

                ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
                verify(transactionRepository, times(1)).save(transactionCaptor.capture());

                Transaction capturedTransaction = transactionCaptor.getValue();
                assertThat(capturedTransaction.getAccount()).isEqualTo(TestData.ACCOUNT);
                assertThat(capturedTransaction.getCategory()).isEqualTo(TestData.CATEGORY);
                assertThat(capturedTransaction.getUserSub()).isEqualTo(TestData.USER_SUB);
            }

            @Test
            @DisplayName("should create a transaction with provided currency conversion")
            void createTransactionForCurrentUser_shouldCreateTransactionWithProvidedConversion() {
                when(accountService.findCurrentUserAccount(TestData.ACCOUNT_2.getId())).thenReturn(TestData.ACCOUNT_2);

                when(categoryRepository.findByUserSubAndName(
                        TestData.USER_SUB, TestData.CONVERTED_TRANSACTION_REQUEST.getCategoryName().getName()
                )).thenReturn(Optional.of(TestData.CATEGORY));

                when(userService.getUserSub()).thenReturn(TestData.USER_SUB);

                when(modelMapper.map(
                        eq(TestData.CONVERTED_TRANSACTION_REQUEST),
                        eq(Transaction.class),
                        eq(TestData.USER_SUB),
                        eq(TestData.CATEGORY),
                        eq(TestData.ACCOUNT_2)
                )).thenReturn(TestData.CONVERTED_TRANSACTION);

                ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
                when(transactionRepository.save(transactionCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

                when(modelMapper.map(any(Transaction.class), eq(TransactionResponse.class)))
                        .thenReturn(new TransactionResponse(UUID.randomUUID(), "421.00",
                                TestData.CATEGORY_RESPONSE, null, TestData.ACCOUNT_RESPONSE_2,
                                null, null, null));

                transactionService.createTransactionForCurrentUser(TestData.ACCOUNT_2.getId(),
                        TestData.CONVERTED_TRANSACTION_REQUEST);

                Transaction savedTransaction = transactionCaptor.getValue();

                assertThat(savedTransaction.getAmount()).isEqualByComparingTo(new BigDecimal("421.00"));
                assertThat(savedTransaction.getDetails()).contains("USD->PLN: 4.21");
            }

            @Test
            @DisplayName("should create a transaction with currency conversion")
            void createTransactionForCurrentUser_shouldCreateTransactionWithCurrencyConversion() {
                when(accountService.findCurrentUserAccount(TestData.ACCOUNT_2.getId())).thenReturn(TestData.ACCOUNT_2);

                when(categoryRepository.findByUserSubAndName(
                        TestData.USER_SUB, TestData.CONVERTED_TRANSACTION_REQUEST_2.getCategoryName().getName()
                )).thenReturn(Optional.of(TestData.CATEGORY));

                when(userService.getUserSub()).thenReturn(TestData.USER_SUB);

                ExchangeResponse exchangeResponse = ExchangeResponse.builder()
                        .baseCode(CurrencyCode.EUR.name())
                        .targetCode(CurrencyCode.PLN.name())
                        .conversionRate(new BigDecimal("0.24").toString())
                        .build();
                when(exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.PLN)).thenReturn(exchangeResponse);

                when(modelMapper.map(
                        any(TransactionRequest.class),
                        eq(Transaction.class),
                        anyString(),
                        any(Category.class),
                        any(Account.class)
                )).thenReturn(TestData.CONVERTED_TRANSACTION_2);

                ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
                when(transactionRepository.save(transactionCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

                when(modelMapper.map(any(Transaction.class), eq(TransactionResponse.class)))
                        .thenReturn(TestData.CONVERTED_TRANSACTION_RESPONSE_2);

                transactionService.createTransactionForCurrentUser(TestData.ACCOUNT_2.getId(),
                        TestData.CONVERTED_TRANSACTION_REQUEST_2);

                Transaction savedTransaction = transactionCaptor.getValue();

                assertThat(savedTransaction.getAmount()).isEqualByComparingTo(new BigDecimal("416.00"));
                assertThat(savedTransaction.getDetails()).contains("EUR->PLN: 0.24 - " + TestData.TEST_DATE);
                assertThat(savedTransaction.getAccount().getCurrencyCode()).isEqualTo(CurrencyCode.PLN);
            }
        }
    }

    @Nested
    @DisplayName("Deleting Transactions")
    class DeletingTransactions {

        @Test
        @DisplayName("should delete transaction successfully")
        void deleteCurrentUserTransaction_shouldDeleteTransaction() {
            when(userService.getUserSub()).thenReturn(TestData.USER_SUB);
            when(transactionRepository.findById(TestData.TRANSACTION.getId()))
                    .thenReturn(Optional.of(TestData.TRANSACTION));

            transactionService.deleteCurrentUserTransaction(TestData.TRANSACTION.getId());

            verify(transactionRepository).delete(TestData.TRANSACTION);
        }

        @Test
        @DisplayName("should throw exception when user is not owner")
        void deleteCurrentUserTransaction_shouldThrowException_whenNotOwner() {
            Transaction transaction = TestData.TRANSACTION;
            when(userService.getUserSub()).thenReturn(TestData.USER_SUB_2);
            when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));

            assertThatThrownBy(() -> transactionService.deleteCurrentUserTransaction(transaction.getId()))
                    .isInstanceOf(UserIsNotOwnerException.class);
        }
    }

    @Nested
    @DisplayName("Image Handling Tests")
    class ImageHandlingTests {

        @Test
        @DisplayName("should add image to transaction successfully for premium user")
        void addImage_shouldSucceed_forPremiumUser() {
            MockMultipartFile imageFile = new MockMultipartFile("file", "image.jpg",
                    "image/jpeg", "content".getBytes());
            String expectedS3Key = "s3-key-for-image.jpg";

            when(userService.isPremiumUser()).thenReturn(true);
            when(userService.getUserSub()).thenReturn(TestData.USER_SUB);
            when(transactionRepository.findById(TestData.TRANSACTION.getId())).thenReturn(Optional.of(TestData.TRANSACTION));
            when(s3Service.uploadFile(imageFile, TestData.USER_SUB, TestData.TRANSACTION.getId())).thenReturn(expectedS3Key);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            transactionService.addImageToCurrentUserTransaction(TestData.TRANSACTION.getId(), imageFile);

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            assertThat(captor.getValue().getImageFilePath()).isEqualTo(expectedS3Key);
        }

        @Test
        @DisplayName("should throw exception when adding image for non-premium user")
        void addImage_shouldFail_forNonPremiumUser() {
            UUID transactionId = UUID.randomUUID();
            MockMultipartFile file = new MockMultipartFile("file", "image.jpg",
                    "image/jpeg", "content".getBytes());
            when(userService.isPremiumUser()).thenReturn(false);

            assertThatThrownBy(() -> transactionService.addImageToCurrentUserTransaction(transactionId, file))
                    .isInstanceOf(PremiumStatusRequiredException.class);

            verify(s3Service, never()).uploadFile(any(), anyString(), any());
        }

        @Test
        @DisplayName("should throw exception when file is not an image")
        void addImage_shouldFail_forInvalidFileType() {
            UUID transactionId = UUID.randomUUID();
            MockMultipartFile file = new MockMultipartFile("file", "document.txt",
                    "text/plain", "content".getBytes());
            when(userService.isPremiumUser()).thenReturn(true);

            assertThatThrownBy(() -> transactionService.addImageToCurrentUserTransaction(transactionId, file))
                    .isInstanceOf(WrongFileTypeException.class);
        }

        @Test
        @DisplayName("should delete image from transaction successfully")
        void deleteImage_shouldSucceed() {
            // Arrange
            String s3Key = "existing-image-key.jpg";
            Transaction transaction = TestData.TRANSACTION;
            transaction.setImageFilePath(s3Key);

            when(userService.getUserSub()).thenReturn(TestData.USER_SUB);
            when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

            transactionService.deleteImageFromCurrentUserTransaction(transaction.getId());

            verify(s3Service, times(1)).deleteFile(s3Key);

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionRepository).save(captor.capture());
            assertThat(captor.getValue().getImageFilePath()).isNull();
        }

        @Test
        @DisplayName("should do nothing when deleting image from transaction that has no image")
        void deleteImage_shouldDoNothing_whenImageIsNull() {
            when(userService.getUserSub()).thenReturn(TestData.USER_SUB);
            when(transactionRepository.findById(TestData.TRANSACTION.getId()))
                    .thenReturn(Optional.of(TestData.TRANSACTION));

            transactionService.deleteImageFromCurrentUserTransaction(TestData.TRANSACTION.getId());

            verify(s3Service, never()).deleteFile(anyString());
            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    @Nested
    @DisplayName("CSV Export Tests")
    class CsvExportTests {

        @Test
        @DisplayName("should generate correct CSV for user with transactions")
        void generateCSV_shouldSucceed_forUserWithTransactions() {
            Transaction transaction1 = TestData.TRANSACTION;
            Transaction transaction2 = TestData.CONVERTED_TRANSACTION_2;
            List<Transaction> userTransactions = List.of(transaction1, transaction2);

            TransactionResponse response1 = TestData.TRANSACTION_RESPONSE;
            TransactionResponse response2 = TestData.CONVERTED_TRANSACTION_RESPONSE_2;

            when(userService.getUserSub()).thenReturn(TestData.USER_SUB);
            when(transactionRepository.findAll(any(Specification.class))).thenReturn(userTransactions);

            when(imageService.getImageUrl(any(Transaction.class))).thenReturn(null);
            when(modelMapper.map(transaction1, TransactionResponse.class)).thenReturn(response1);
            when(modelMapper.map(transaction2, TransactionResponse.class)).thenReturn(response2);

            String expectedCsvContent = "sep=,\n" +
                    "Account name,Currency code,Amount,Category,Date,Transaction method,Description\n" +
                    "USD account,USD,100.00,Food,2025-07-15,CASH,\"\"\n" +
                    "PLN account,PLN,416.00,Food,2025-07-15,CASH,\"EUR->PLN: 0.24 - 2025-07-15\"\n";

            byte[] csvBytes = transactionService.generateCSVWithCurrentUserTransactions();
            String actualCsvContent = new String(csvBytes, StandardCharsets.UTF_8);

            assertThat(actualCsvContent).isEqualTo(expectedCsvContent);
        }

        @Test
        @DisplayName("should generate CSV with only header for user with no transactions")
        void generateCSV_shouldReturnOnlyHeader_forUserWithNoTransactions() {
            when(userService.getUserSub()).thenReturn(TestData.USER_SUB);
            when(transactionRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

            String expectedCsvContent = "sep=,\n" +
                    "Account name,Currency code,Amount,Category,Date,Transaction method,Description\n";

            byte[] csvBytes = transactionService.generateCSVWithCurrentUserTransactions();
            String actualCsvContent = new String(csvBytes, StandardCharsets.UTF_8);

            assertThat(actualCsvContent).isEqualTo(expectedCsvContent);
        }
    }
}