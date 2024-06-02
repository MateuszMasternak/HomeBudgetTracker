package com.rainy.homebudgettracker.service;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.TransactionService;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class TransactionServiceTest {
    private Page<Transaction> transactions1, transactions2;
    private User user1, user2;
    private Category category1, category2;

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

        category1 = Category.builder()
                .id(1L)
                .name("Category")
                .user(user1)
                .build();

        category2 = Category.builder()
                .id(2L)
                .name("Category2")
                .user(user1)
                .build();

        transactions1 = new PageImpl<>(List.of(
                Transaction.builder()
                        .id(1L)
                        .amount(BigDecimal.TEN)
                        .category(category1)
                        .date(LocalDate.of(2024, 1, 1))
                        .user(user1)
                        .build(),
                Transaction.builder()
                        .id(2L)
                        .amount(BigDecimal.TEN)
                        .category(category2)
                        .date(LocalDate.of(2024, 6, 1))
                        .user(user1)
                        .build()
        ));

        transactions2 = new PageImpl<>(List.of());
    }

    @AfterEach
    void tearDown() {
        transactions1 = null;
        transactions2 = null;
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
                .willReturn(transactions1);

        given(transactionRepository.findAllByUser(user2, Pageable.ofSize(10).withPage(1)))
                .willReturn(transactions2);

        Page<TransactionResponse> page1 =  transactionService.findAllByUser(
                user1, Pageable.ofSize(10).withPage(0));
        assert page1.getTotalElements() == 2;

        assert page1.getContent().get(0).getId() == 1L;
        assert page1.getContent().get(0).getAmount().equals("10");
        assert page1.getContent().get(0).getCategory().getId() == 1L;
        assert page1.getContent().get(0).getCategory().getName().equals("Category");
        assert page1.getContent().get(0).getDate().equals("2024-01-01");

        assert page1.getContent().get(1).getId() == 2L;
        assert page1.getContent().get(1).getAmount().equals("10");
        assert page1.getContent().get(1).getCategory().getId() == 2L;
        assert page1.getContent().get(1).getCategory().getName().equals("Category2");
        assert page1.getContent().get(1).getDate().equals("2024-06-01");

        Page<TransactionResponse> page2 =  transactionService.findAllByUser(
                user2, Pageable.ofSize(10).withPage(0));
        assert page2.getTotalElements() == 0;
    }


}
