package com.rainy.homebudgettracker.controller;

import com.rainy.homebudgettracker.auth.JwtService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.transaction.TransactionController;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.TransactionService;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtService jwtService;

    private User user;

    @BeforeEach
    public void setup() {
        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .password("password")
                .enabled(true)
                .role(Role.USER)
                .build();

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), user.getAuthorities());
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterAll
    public static void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getAllTransactionsByUserReturn200() throws Exception {
        when(transactionService.findAllByUser(any(User.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/transaction?page=0&size=10")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void getAllTransactionsByUserReturn401IfNotLoggedIn() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/transaction?page=0&size=10")
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAllTransactionsByUserAndCategoryReturn200() throws Exception {
        when(transactionService.findAllByUserAndCategory(any(User.class), any(String.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/transaction/category?categoryName=category")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void getAllTransactionsByUserAndCategoryReturn401IfNotLoggedIn() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/transaction/category?categoryName=category")
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAllTransactionsByUserAndCategoryReturn404IfCategoryDoesNotExist() throws Exception {
        when(transactionService.findAllByUserAndCategory(any(User.class), any(String.class), any(Pageable.class)))
                .thenThrow(new RecordDoesNotExistException("Category does not exist"));

        mockMvc.perform(get("/api/v1/transaction/category?categoryName=category")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAllTransactionsByUserAndDateBetweenReturn200() throws Exception {
        when(transactionService.findAllByUserAndDateBetween(
                any(User.class), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/transaction/date?startDate=2024-01-01&endDate=2024-12-31")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void getAllTransactionsByUserAndDateBetweenReturn401IfNotLoggedIn() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/transaction/date?startDate=2024-01-01&endDate=2024-12-31")
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAllTransactionsByUserAndCategoryAndDateBetweenReturn200() throws Exception {
        when(transactionService.findAllByUserAndCategoryAndDateBetween(
                any(User.class), any(String.class), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/transaction/category-date" +
                        "?categoryName=category&startDate=2024-01-01&endDate=2024-12-31")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void getAllTransactionsByUserAndCategoryAndDateBetweenReturn401IfNotLoggedIn() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/transaction/category-date" +
                        "?categoryName=category&startDate=2024-01-01&endDate=2024-12-31")
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAllTransactionsByUserAndCategoryAndDateBetweenReturn404IfCategoryDoesNotExist() throws Exception {
        when(transactionService.findAllByUserAndCategoryAndDateBetween(
                any(User.class), any(String.class), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenThrow(new RecordDoesNotExistException("Category does not exist"));

        mockMvc.perform(get("/api/v1/transaction/category-date" +
                        "?categoryName=category&startDate=2024-01-01&endDate=2024-12-31")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createTransactionReturn200() throws Exception {
        when(transactionService.createTransaction(any(User.class), any(TransactionRequest.class)))
                .thenReturn(TransactionResponse.builder().id(1L).build());

        mockMvc.perform(post("/api/v1/transaction")
                        .with(user(user))
                        .with(csrf())
                        .content("{\"category\": {\"name\": \"category\"}, \"amount\": 100, \"date\": \"2024-01-01\"}")
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void createTransactionReturn401IfNotLoggedIn() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/v1/transaction")
                        .with(csrf())
                        .content("{\"category\": {\"name\": \"category\"}, \"amount\": 100, \"date\": \"2024-01-01\"}")
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void createTransactionReturn404IfCategoryDoesNotExist() throws Exception {
        when(transactionService.createTransaction(any(User.class), any(TransactionRequest.class)))
                .thenThrow(new RecordDoesNotExistException("Category does not exist"));

        mockMvc.perform(post("/api/v1/transaction")
                        .with(user(user))
                        .with(csrf())
                        .content("{\"category\": {\"name\": \"category\"}, \"amount\": 100, \"date\": \"2024-01-01\"}")
                        .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTransactionReturn204() throws Exception {
        doNothing().when(transactionService).deleteTransaction(any(User.class), any(Long.class));

        mockMvc.perform(delete("/api/v1/transaction/1")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTransactionReturn401IfNotLoggedIn() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/v1/transaction/1")
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteTransactionReturn403IfUserIsNotOwner() throws Exception {
        doThrow(UserIsNotOwnerException.class)
                .when(transactionService).deleteTransaction(any(User.class), eq(1L));

        mockMvc.perform(delete("/api/v1/transaction/1")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteTransactionReturn404IfTransactionDoesNotExist() throws Exception {
        doThrow(RecordDoesNotExistException.class)
                .when(transactionService).deleteTransaction(any(User.class), eq(1L));

        mockMvc.perform(delete("/api/v1/transaction/1")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void sumPositiveTransactionsReturn200() throws Exception {
        when(transactionService.sumPositiveAmountByUser(any(User.class)))
                .thenReturn("0");

        mockMvc.perform(get("/api/v1/transaction/sum-positive")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void sumPositiveTransactionsReturn401IfNotLoggedIn() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/transaction/sum-positive")
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void sumNegativeTransactionsReturn200() throws Exception {
        when(transactionService.sumNegativeAmountByUser(any(User.class)))
                .thenReturn("0");

        mockMvc.perform(get("/api/v1/transaction/sum-negative")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void sumNegativeTransactionsReturn401IfNotLoggedIn() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/transaction/sum-negative")
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void sumTransactionsReturn200() throws Exception {
        when(transactionService.sumAmountByUser(any(User.class)))
                .thenReturn("0");

        mockMvc.perform(get("/api/v1/transaction/sum")
                        .with(user(user))
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @Test
    public void sumTransactionsReturn401IfNotLoggedIn() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/transaction/sum")
                        .with(csrf())
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized());
    }
}
