package com.rainy.homebudgettracker.user;

import com.rainy.homebudgettracker.account.AccountRepository;
import com.rainy.homebudgettracker.category.CategoryRepository;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final TokenRepository tokenRepository;

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional
    public void deleteUser() {
        User user = getCurrentUser();
        accountRepository.deleteAllByUser(user);
        categoryRepository.deleteAllByUser(user);
        transactionRepository.deleteAllByUser(user);
        tokenRepository.deleteAllByUser(user);
        userRepository.delete(user);
    }
}
