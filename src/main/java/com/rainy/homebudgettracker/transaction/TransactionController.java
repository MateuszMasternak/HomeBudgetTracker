package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Iterable<Transaction>> getAllTransactionsByUser(Principal principal) {
        return ResponseEntity.ok(transactionService.findAllByUser((User) principal));
    }

    @GetMapping("/category")
    public ResponseEntity<Iterable<Transaction>> getAllTransactionsByUserAndCategory(
            Principal principal,
            @RequestParam Category category
    ) {
        return ResponseEntity.ok(transactionService.findAllByUserAndCategory((User) principal, category));
    }

    @GetMapping("/date")
    public ResponseEntity<Iterable<Transaction>> getAllTransactionsByUserAndDateBetween(
            Principal principal,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        return ResponseEntity.ok(transactionService.findAllByUserAndDateBetween(
                (User) principal,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        ));
    }

    @GetMapping("/category-date")
    public ResponseEntity<Iterable<Transaction>> getAllTransactionsByUserAndCategoryAndDateBetween(
            Principal principal,
            @RequestParam Category category,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        return ResponseEntity.ok(transactionService.findAllByUserAndCategoryAndDateBetween(
                (User) principal,
                category,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        ));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            Principal principal,
            @RequestBody TransactionRequest transactionRequest
    ) {
        return ResponseEntity.ok(transactionService.createTransaction((User) principal, transactionRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            Principal principal,
            @PathVariable Long id
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {
        transactionService.deleteTransaction((User) principal, id);
        return ResponseEntity.noContent().build();
    }
}
