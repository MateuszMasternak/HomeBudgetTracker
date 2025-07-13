//package com.rainy.homebudgettracker.transaction;
//
//import com.rainy.homebudgettracker.account.AccountService;
//import com.rainy.homebudgettracker.category.CategoryService;
//import com.rainy.homebudgettracker.exchange.ExchangeService;
//import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
//import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
//import com.rainy.homebudgettracker.images.ImageService;
//import com.rainy.homebudgettracker.mapper.ModelMapper;
//import com.rainy.homebudgettracker.transaction.service.TransactionAggregationServiceImpl;
//import com.rainy.homebudgettracker.transaction.service.TransactionServiceImpl;
//import com.rainy.homebudgettracker.user.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.when;
//
//public class TransactionAggregationServiceTest {
//    @InjectMocks
//    TransactionAggregationServiceImpl transactionService;
//    @Mock
//    TransactionRepository transactionRepository;
//    @Mock
//    CategoryService categoryService;
//    @Mock
//    AccountService accountService;
//    @Mock
//    ExchangeService exchangeService;
//    @Mock
//    ModelMapper modelMapper;
//    @Mock
//    UserService userService;
//    @Mock
//    ImageService imageService;
//
//    @BeforeEach
//    void setUp() throws RecordDoesNotExistException, UserIsNotOwnerException {
//        MockitoAnnotations.openMocks(this);
//
//        when(accountService.findCurrentUserAccount(UUID.fromString(TestData.ACCOUNT_ID))).thenReturn(TestData.ACCOUNT);
//        when(categoryService.findCurrentUserCategory("Food")).thenReturn(TestData.CATEGORY);
//
//        when(modelMapper.map(BigDecimal.valueOf(100.1).setScale(2, RoundingMode.HALF_UP), SumResponse.class)).thenReturn(TestData.SUM_RESPONSE);
//        when(modelMapper.map(BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), SumResponse.class)).thenReturn(TestData.SUM_RESPONSE_2);
//
//        when(transactionRepository.sumPositiveAmountByAccount(TestData.ACCOUNT)).thenReturn(BigDecimal.valueOf(100.1));
//        when(transactionRepository.sumPositiveAmountByAccount(TestData.ACCOUNT_2)).thenReturn(BigDecimal.valueOf(0));
//        when(transactionRepository.sumNegativeAmountByAccount(TestData.ACCOUNT)).thenReturn(BigDecimal.valueOf(100.1));
//        when(transactionRepository.sumNegativeAmountByAccount(TestData.ACCOUNT_2)).thenReturn(BigDecimal.valueOf(0));
//        when(transactionRepository.sumAmountByAccount(TestData.ACCOUNT)).thenReturn(BigDecimal.valueOf(100.1));
//        when(transactionRepository.sumAmountByAccount(TestData.ACCOUNT_2)).thenReturn(BigDecimal.valueOf(0));
//    }
//
//    @Test
//    void shouldReturnSumPositiveAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
//        var sumPositiveAmount = transactionService.sumCurrentUserPositiveAmount(UUID.fromString(TestData.ACCOUNT_ID));
//
//        assertEquals(TestData.SUM_RESPONSE, sumPositiveAmount);
//    }
//
//    @Test
//    void shouldReturnSumAs0PositiveAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
//        var sumPositiveAmount = transactionService.sumCurrentUserPositiveAmount(UUID.fromString(TestData.ACCOUNT_ID_2));
//
//        assertEquals(TestData.SUM_RESPONSE_2, sumPositiveAmount);
//
//    }
//
//    @Test
//    void shouldReturnSumNegativeAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
//        var sumNegativeAmount = transactionService.sumCurrentUserNegativeAmount(UUID.fromString(TestData.ACCOUNT_ID));
//
//        assertEquals(TestData.SUM_RESPONSE, sumNegativeAmount);
//    }
//
//    @Test
//    void shouldReturnSumAs0NegativeAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
//        var sumPositiveAmount = transactionService.sumCurrentUserNegativeAmount(UUID.fromString(TestData.ACCOUNT_ID_2));
//
//        assertEquals(TestData.SUM_RESPONSE_2, sumPositiveAmount);
//    }
//
//    @Test
//    void shouldReturnSumAllAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
//        var sumAllAmount = transactionService.sumCurrentUserAmount(UUID.fromString(TestData.ACCOUNT_ID));
//
//        assertEquals(TestData.SUM_RESPONSE, sumAllAmount);
//    }
//
//    @Test
//    void shouldReturnSumAs0AllAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
//        var sumPositiveAmount = transactionService.sumCurrentUserAmount(UUID.fromString(TestData.ACCOUNT_ID_2));
//
//        assertEquals(TestData.SUM_RESPONSE_2, sumPositiveAmount);
//    }
//}
