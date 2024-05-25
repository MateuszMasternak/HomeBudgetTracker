package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
