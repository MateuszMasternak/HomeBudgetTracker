package com.rainy.homebudgettracker.service;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.transaction.*;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class TransactionServiceTest {
    private Page<Transaction> transactionsByUser1;
    private Page<Transaction> transactionByUserAndCategory1, transactionByUserAndCategory2;
    private Page<Transaction> transactionsByUserAndDate1, transactionsByUserAndDate2;
    private Page<Transaction> transactionByUserAndCategoryAndDate1, transactionByUserAndCategoryAndDate2;
    private User user1, user2, user3;
    private Category category1, category2, category3;
    private Transaction transaction1, transaction2, transaction3, transaction4;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .firstName("User")
                .email("user")
                .password("password")
                .role(Role.USER)
                .accountLocked(false)
                .enabled(true)
                .build();

        user2 = User.builder()
                .id(2L)
                .firstName("User2")
                .email("user2")
                .password("password")
                .role(Role.USER)
                .accountLocked(false)
                .enabled(true)
                .build();

        user3 = User.builder()
                .id(3L)
                .firstName("User3")
                .email("user3")
                .password("password")
                .role(Role.USER)
                .accountLocked(false)
                .enabled(true)
                .build();

        category1 = Category.builder()
                .id(1L)
                .name("CATEGORY")
                .user(user1)
                .build();

        category2 = Category.builder()
                .id(2L)
                .name("CATEGORY2")
                .user(user1)
                .build();

        category3 = Category.builder()
                .id(3L)
                .name("CATEGORY")
                .user(user2)
                .build();

        transaction1 = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.TEN)
                .category(category1)
                .date(LocalDate.of(2024, 1, 1))
                .user(user1)
                .build();

        transaction2 = Transaction.builder()
                .id(2L)
                .amount(BigDecimal.TEN)
                .category(category1)
                .date(LocalDate.of(2024, 6, 1))
                .user(user1)
                .build();

        transaction3 = Transaction.builder()
                .id(3L)
                .amount(BigDecimal.TEN)
                .category(category2)
                .date(LocalDate.of(2024, 3, 1))
                .user(user1)
                .build();

        transaction4 = Transaction.builder()
                .id(4L)
                .amount(BigDecimal.TEN)
                .category(
                        Category.builder()
                                .id(4L)
                                .name("CATEGORY")
                                .user(user3)
                                .build()
                )
                .date(LocalDate.of(2024, 6, 1))
                .user(user3)
                .build();

        transactionsByUser1 = new PageImpl<>(List.of(
                transaction1,
                transaction2,
                transaction3
        ));

        transactionsByUserAndDate1 = new PageImpl<>(List.of(
                transaction1,
                transaction2,
                transaction3
        ));

        transactionsByUserAndDate2 = new PageImpl<>(List.of(
                transaction3
        ));

        transactionByUserAndCategory1 = new PageImpl<>(List.of(
                transaction1,
                transaction2
        ));

        transactionByUserAndCategory2 = new PageImpl<>(List.of(
                Transaction.builder()
                        .id(1L)
                        .amount(BigDecimal.TEN)
                        .category(category2)
                        .date(LocalDate.of(2024, 6, 1))
                        .user(user1)
                        .build()
        ));

        transactionByUserAndCategoryAndDate1 = new PageImpl<>(List.of(
                transaction1,
                transaction2
        ));

        transactionByUserAndCategoryAndDate2 = new PageImpl<>(List.of(
                transaction3
        ));
    }

    @AfterEach
    void tearDown() {
        transactionsByUser1 = null;
        transactionByUserAndCategory1 = null;
        transactionByUserAndCategory2 = null;
        transactionsByUserAndDate1 = null;
        transactionsByUserAndDate2 = null;
        transactionByUserAndCategoryAndDate1 = null;
        transactionByUserAndCategoryAndDate2 = null;
        transaction1 = null;
        transaction2 = null;
        transaction3 = null;
        category1 = null;
        category2 = null;
        user1 = null;
        user2 = null;
    }

    @Test
    void findAllByUser() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CategoryService categoryService = mock(CategoryService.class);

        TransactionService transactionService = new TransactionService(transactionRepository, categoryService);

        given(transactionRepository.findAllByUser(user1, Pageable.ofSize(10).withPage(0)))
                .willReturn(transactionsByUser1);
        given(transactionRepository.findAllByUser(user2, Pageable.ofSize(10).withPage(0)))
                .willReturn(Page.empty());

        Page<TransactionResponse> page1 =  transactionService.findAllByUser(
                user1, Pageable.ofSize(10).withPage(0));
        assert page1.getTotalElements() == 3;

        Page<TransactionResponse> page2 =  transactionService.findAllByUser(
                user2, Pageable.ofSize(10).withPage(0));
        assert page2.getTotalElements() == 0;
    }


    @Test
    void findAllByUserAndCategory() throws RecordDoesNotExistException {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CategoryService categoryService = mock(CategoryService.class);

        TransactionService transactionService = new TransactionService(transactionRepository, categoryService);

        given(categoryService.findByUserAndName(user1, "CATEGORY"))
                .willReturn(CategoryResponse.builder().id(1L).name("CATEGORY").build());
        given(categoryService.findByUserAndName(user1, "CATEGORY2"))
                .willReturn(CategoryResponse.builder().id(2L).name("CATEGORY2").build());
        given(categoryService.findByUserAndName(user2, "CATEGORY"))
                .willReturn(CategoryResponse.builder().id(3L).name("CATEGORY").build());
        given(categoryService.findByUserAndName(user1, "CATEGORY3"))
                .willThrow(RecordDoesNotExistException.class);

        Pageable pageable = Pageable.ofSize(10).withPage(0);

        given(transactionRepository.findAllByUserAndCategory(
                user1, category1, pageable))
                .willReturn(transactionByUserAndCategory1);
        given(transactionRepository.findAllByUserAndCategory(
                user1, category2, pageable))
                .willReturn(transactionByUserAndCategory2);
        given(transactionRepository.findAllByUserAndCategory(
                user2, category3, pageable))
                .willReturn(Page.empty());

        Page<TransactionResponse> page1 =  transactionService.findAllByUserAndCategory(
                user1, "CATEGORY", Pageable.ofSize(10).withPage(0));
        assert page1.getTotalElements() == 2;

        Page<TransactionResponse> page2 =  transactionService.findAllByUserAndCategory(
                user1, "CATEGORY2", Pageable.ofSize(10).withPage(0));
        assert page2.getTotalElements() == 1;

        Page<TransactionResponse> page3 =  transactionService.findAllByUserAndCategory(
                user2, "CATEGORY", Pageable.ofSize(10).withPage(0));
        assert page3.getTotalElements() == 0;

        Assertions.assertThrows(RecordDoesNotExistException.class, () ->
                transactionService.findAllByUserAndCategory(
                        user1, "CATEGORY3", Pageable.ofSize(10).withPage(0)));
    }

    @Test
    void findAllByUserAndDateBetween() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CategoryService categoryService = mock(CategoryService.class);

        TransactionService transactionService = new TransactionService(transactionRepository, categoryService);

        given(transactionRepository.findAllByUserAndDateBetween(
                user1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 1),
                Pageable.ofSize(10).withPage(0)))
                .willReturn(transactionsByUserAndDate1);
        given(transactionRepository.findAllByUserAndDateBetween(
                user1,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 4, 1),
                Pageable.ofSize(10).withPage(0)))
                .willReturn(transactionsByUserAndDate2);
        given(transactionRepository.findAllByUserAndDateBetween(
                user2,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 1),
                Pageable.ofSize(10).withPage(0)))
                .willReturn(Page.empty());

        Page<TransactionResponse> page1 =  transactionService.findAllByUserAndDateBetween(
                user1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 1),
                Pageable.ofSize(10).withPage(0));
        assert page1.getTotalElements() == 3;

        Page<TransactionResponse> page2 =  transactionService.findAllByUserAndDateBetween(
                user1,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 4, 1),
                Pageable.ofSize(10).withPage(0));
        assert page2.getTotalElements() == 1;

        Page<TransactionResponse> page3 =  transactionService.findAllByUserAndDateBetween(
                user2,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 1),
                Pageable.ofSize(10).withPage(0));
        assert page3.getTotalElements() == 0;
    }

    @Test
    void findAllByUserAndCategoryAndDateBetween() throws RecordDoesNotExistException {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CategoryService categoryService = mock(CategoryService.class);

        TransactionService transactionService = new TransactionService(transactionRepository, categoryService);

        given(categoryService.findByUserAndName(user1, "CATEGORY"))
                .willReturn(CategoryResponse.builder().id(1L).name("CATEGORY").build());
        given(categoryService.findByUserAndName(user1, "CATEGORY2"))
                .willReturn(CategoryResponse.builder().id(2L).name("CATEGORY2").build());
        given(categoryService.findByUserAndName(user2, "CATEGORY"))
                .willReturn(CategoryResponse.builder().id(3L).name("CATEGORY").build());
        given(categoryService.findByUserAndName(user1, "CATEGORY3"))
                .willThrow(RecordDoesNotExistException.class);

        Pageable pageable = Pageable.ofSize(10).withPage(0);

        given(transactionRepository.findAllByUserAndCategoryAndDateBetween(
                user1,
                category1,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 1),
                pageable))
                .willReturn(transactionByUserAndCategoryAndDate1);
        given(transactionRepository.findAllByUserAndCategoryAndDateBetween(
                user1,
                category2,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 1),
                pageable))
                .willReturn(transactionByUserAndCategoryAndDate2);
        given(transactionRepository.findAllByUserAndCategoryAndDateBetween(
                user2,
                category3,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 1),
                pageable))
                .willReturn(Page.empty());

        Page<TransactionResponse> page1 = transactionService.findAllByUserAndCategoryAndDateBetween(
                user1,
                "CATEGORY",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 1),
                Pageable.ofSize(10).withPage(0));
        assert page1.getTotalElements() == 2;

        Page<TransactionResponse> page2 = transactionService.findAllByUserAndCategoryAndDateBetween(
                user1,
                "CATEGORY2",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 1),
                Pageable.ofSize(10).withPage(0));
        assert page2.getTotalElements() == 1;

        Page<TransactionResponse> page3 = transactionService.findAllByUserAndCategoryAndDateBetween(
                user2,
                "CATEGORY",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 1),
                Pageable.ofSize(10).withPage(0));
        assert page3.getTotalElements() == 0;

        Assertions.assertThrows(RecordDoesNotExistException.class, () ->
                transactionService.findAllByUserAndCategoryAndDateBetween(
                        user1,
                        "CATEGORY3",
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 6, 1),
                        Pageable.ofSize(10).withPage(0)));
    }

    @Test
    void createTransaction() throws RecordDoesNotExistException {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CategoryService categoryService = mock(CategoryService.class);

        TransactionService transactionService = new TransactionService(transactionRepository, categoryService);

        given(categoryService.findByUserAndName(user1, "CATEGORY"))
                .willReturn(CategoryResponse.builder().id(1L).name("CATEGORY").build());
        given(categoryService.findByUserAndName(user1, "Category2"))
                .willReturn(CategoryResponse.builder().id(2L).name("CATEGORY2").build());
        given(categoryService.findByUserAndName(user2, "CATEGORY"))
                .willReturn(CategoryResponse.builder().id(3L).name("CATEGORY").build());
        given(categoryService.findByUserAndName(user1, "CATEGORY3"))
                .willThrow(RecordDoesNotExistException.class);

        given(transactionRepository.save(
                Transaction.builder()
                        .amount(BigDecimal.TEN)
                        .category(category1)
                        .date(LocalDate.parse("2024-01-01"))
                        .user(user1)
                        .build()))
                .willReturn(transaction1);

        TransactionResponse transactionResponse = transactionService.createTransaction(
                user1,
                TransactionRequest.builder()
                        .amount(BigDecimal.TEN)
                        .category(CategoryRequest.builder().name("CATEGORY").build())
                        .date(LocalDate.parse("2024-01-01"))
                        .build()
        );

        assert transactionResponse.getId() == 1L;
        assert transactionResponse.getAmount().equals("10");
        assert transactionResponse.getCategory().getId() == 1L;
        assert transactionResponse.getCategory().getName().equals("CATEGORY");
        assert transactionResponse.getDate().equals("2024-01-01");

        Assertions.assertThrows(RecordDoesNotExistException.class, () ->
                transactionService.createTransaction(
                        user1,
                        TransactionRequest.builder()
                                .amount(BigDecimal.TEN)
                                .category(CategoryRequest.builder().name("CATEGORY3").build())
                                .date(LocalDate.parse("2024-01-01"))
                                .build()
                ));
    }

    @Test
    void deleteTransaction() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CategoryService categoryService = mock(CategoryService.class);

        TransactionService transactionService = new TransactionService(transactionRepository, categoryService);

        given(transactionRepository.existsById(1L))
                .willReturn(true);
        given(transactionRepository.findById(1L))
                .willReturn(Optional.of(transaction1));

        given(transactionRepository.existsById(4L))
                .willReturn(true);
        given(transactionRepository.findById(4L))
                .willReturn(Optional.of(transaction4));

        given(transactionRepository.existsById(5L))
                .willReturn(false);

        Assertions.assertDoesNotThrow(() ->
                transactionService.deleteTransaction(user1, 1L));
        Assertions.assertThrows(UserIsNotOwnerException.class, () ->
                transactionService.deleteTransaction(user1, 4L));
        Assertions.assertThrows(RecordDoesNotExistException.class, () ->
                transactionService.deleteTransaction(user1, 5L));
    }

    @Test
    void existsByCategory() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CategoryService categoryService = mock(CategoryService.class);

        TransactionService transactionService = new TransactionService(transactionRepository, categoryService);

        given(transactionRepository.existsByCategory(category1))
                .willReturn(true);
        given(transactionRepository.existsByCategory(category2))
                .willReturn(false);

        assert transactionService.existsByCategory(category1);
        assert !transactionService.existsByCategory(category2);
    }

    @Test
    void sumPositiveAmountByUser() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CategoryService categoryService = mock(CategoryService.class);

        TransactionService transactionService = new TransactionService(transactionRepository, categoryService);

        given(transactionRepository.sumPositiveAmountByUser(user1))
                .willReturn(BigDecimal.TEN);
        given(transactionRepository.sumPositiveAmountByUser(user2))
                .willReturn(null);

        assert transactionService.sumPositiveAmountByUser(user1).equals("10");
        assert transactionService.sumPositiveAmountByUser(user2).equals("0");
    }

    @Test
    void sumNegativeAmountByUser() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CategoryService categoryService = mock(CategoryService.class);

        TransactionService transactionService = new TransactionService(transactionRepository, categoryService);

        given(transactionRepository.sumNegativeAmountByUser(user1))
                .willReturn(BigDecimal.TEN);
        given(transactionRepository.sumNegativeAmountByUser(user2))
                .willReturn(null);

        assert transactionService.sumNegativeAmountByUser(user1).equals("10");
        assert transactionService.sumNegativeAmountByUser(user2).equals("0");
    }

    @Test
    void sumAmountByUser() {
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        CategoryService categoryService = mock(CategoryService.class);

        TransactionService transactionService = new TransactionService(transactionRepository, categoryService);

        given(transactionRepository.sumAmountByUser(user1))
                .willReturn(BigDecimal.TEN);
        given(transactionRepository.sumAmountByUser(user2))
                .willReturn(null);

        assert transactionService.sumAmountByUser(user1).equals("10");
        assert transactionService.sumAmountByUser(user2).equals("0");
    }
}
