package com.rainy.homebudgettracker.user;

import com.rainy.homebudgettracker.account.AccountRepository;
import com.rainy.homebudgettracker.category.CategoryRepository;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public String getUserSub() {
        return String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    public boolean isPremiumUser() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.PREMIUM_USER.name()));
    }

    @Transactional
    public void deleteUserData() {
        String sub = getUserSub();
        accountRepository.deleteAllByUserSub(sub);
        categoryRepository.deleteAllByUserSub(sub);
        transactionRepository.deleteAllByUserSub(sub);
    }
}
