package com.rainy.homebudgettracker.auth;

import com.rainy.homebudgettracker.handler.exception.EmailAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.EmailAlreadyInUseException;
import com.rainy.homebudgettracker.handler.exception.ExpiredConfirmationTokenException;
import com.rainy.homebudgettracker.handler.exception.InvalidConfirmationTokenException;
import jakarta.mail.MessagingException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AuthenticationService {
    void register(RegisterRequest registerRequest) throws MessagingException, EmailAlreadyExistsException;
    public void sendPasswordResetEmail(PasswordResetLinkRequest email)
            throws MessagingException, UsernameNotFoundException;
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    void activateAccount(String token)
            throws MessagingException, InvalidConfirmationTokenException, ExpiredConfirmationTokenException;
    void changePassword(String token, ChangePasswordRequest changePasswordRequest)
            throws InvalidConfirmationTokenException, ExpiredConfirmationTokenException;
    void changePassword(ChangePasswordRequest changePasswordRequest);
    void changeEmail(ChangeEmailRequest changeEmailRequest) throws EmailAlreadyInUseException;

}
