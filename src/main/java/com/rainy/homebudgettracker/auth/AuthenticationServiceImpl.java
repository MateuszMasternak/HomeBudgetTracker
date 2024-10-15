package com.rainy.homebudgettracker.auth;

import com.rainy.homebudgettracker.email.EmailService;
import com.rainy.homebudgettracker.email.EmailTemplateName;
import com.rainy.homebudgettracker.handler.exception.EmailAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.EmailAlreadyInUseException;
import com.rainy.homebudgettracker.handler.exception.ExpiredConfirmationTokenException;
import com.rainy.homebudgettracker.handler.exception.InvalidConfirmationTokenException;
import com.rainy.homebudgettracker.user.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final UserService userService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;
    @Value("${application.mailing.frontend.reset-url}")
    private String resetUrl;

    @Override
    public void register(RegisterRequest registerRequest) throws MessagingException, EmailAlreadyExistsException {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.info("Email already exists: {}", registerRequest.getEmail());
            throw new EmailAlreadyExistsException("Email already exists");
        }

        var user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .enabled(false)
                .accountLocked(false)
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveToken(user);
        emailService.sendConfirmationEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl + "?token=" + newToken,
                "Account activation"
        );
    }

    @Override
    public void sendPasswordResetEmail(PasswordResetLinkRequest email)
            throws MessagingException, UsernameNotFoundException
    {
        var user = userRepository.findByEmail(email.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var newToken = generateAndSaveToken(user);
        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.PASSWORD_RESET,
                resetUrl + "?token=" + newToken,
                "Password reset"
        );
    }

    private String generateAndSaveToken(User user) {
        var generatedToken = generateActivationToken(6);
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .user(user)
                .build();
        tokenRepository.save(token);
        return generatedToken;
    }

    private String generateActivationToken(int length) {
        String characters = "0123456789";
        var tokenBuilder = new StringBuilder();
        var random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characters.length());
            tokenBuilder.append(characters.charAt(randomIndex));
        }
        return tokenBuilder.toString();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );
        var claims = new HashMap<String, Object>();
        var user = (User) auth.getPrincipal();
        claims.put("fullName", user.getFullName());
        var jwtToken = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    @Transactional
    @Override
    public void activateAccount(String token)
            throws MessagingException, InvalidConfirmationTokenException, ExpiredConfirmationTokenException
    {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidConfirmationTokenException("Invalid token"));
        if (savedToken.getConfirmedAt() != null) {
            return;
        }
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new ExpiredConfirmationTokenException(
                    "Token expired. New token has been sent to the same email address");
        }
        var user = userRepository.findById(Math.toIntExact(savedToken.getUser().getId()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        savedToken.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

    @Transactional
    @Override
    public void changePassword(String token, ChangePasswordRequest changePasswordRequest)
            throws InvalidConfirmationTokenException, ExpiredConfirmationTokenException
    {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidConfirmationTokenException("Invalid token"));
        if (savedToken.getConfirmedAt() != null) {
            return;
        }
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            throw new ExpiredConfirmationTokenException(
                    "Token expired. New token has been sent to the same email address");
        }
        var user = userRepository.findById(Math.toIntExact(savedToken.getUser().getId()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getPassword()));
        userRepository.save(user);
        savedToken.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = userService.getCurrentUser();
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getPassword()));
        userRepository.save(user);
    }

    @Override
    public void changeEmail(ChangeEmailRequest changeEmailRequest) throws EmailAlreadyInUseException {
        User user = userService.getCurrentUser();

        if (userRepository.existsByEmail(changeEmailRequest.getEmail())) {
            throw new EmailAlreadyInUseException("Email already in use");
        }

        user.setEmail(changeEmailRequest.getEmail());
        userRepository.save(user);
    }
}
