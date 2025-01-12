package com.rainy.homebudgettracker.user;

import com.rainy.homebudgettracker.account.AccountRepository;
import com.rainy.homebudgettracker.category.CategoryRepository;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.DeleteUserResponse;

@Service
@AllArgsConstructor
public class UserService {
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CognitoIdentityProviderClient cognitoClient;

    public String getUserSub() {
        return String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    public String getAccessToken() {
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }

    public boolean isPremiumUser() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.PREMIUM_USER.name()));
    }

    @Transactional
    public void deleteCognitoUser() {
        String token = getAccessToken();
        DeleteUserRequest request = DeleteUserRequest.builder()
                .accessToken(token)
                .build();
        DeleteUserResponse response = cognitoClient.deleteUser(request);
        if (response.sdkHttpResponse().isSuccessful()) {
            deleteUserData();
        }
    }

    @Transactional
    public void deleteUserData() {
        String sub = getUserSub();
        accountRepository.deleteAllByUserSub(sub);
        categoryRepository.deleteAllByUserSub(sub);
        transactionRepository.deleteAllByUserSub(sub);
    }

    public UserInfoResponse getUserInfo() {
        return UserInfoResponse.builder()
                .premiumUser(isPremiumUser())
                .build();
    }
}
