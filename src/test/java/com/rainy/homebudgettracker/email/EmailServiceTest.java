package com.rainy.homebudgettracker.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    JavaMailSender mailSender;

    @Mock
    SpringTemplateEngine templateEngine;

    @InjectMocks
    EmailService emailService;

    @Mock
    MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(emailService, "emailFrom", "test@test.com");
    }

    @Test
    void shouldSendEmailWithCorrectTemplateConfirmationEmail() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String username = "JohnDoe";
        String confirmationUrl = "http://test.com/confirm";
        String subject = "Confirm your email";
        String templateName = "activate_account_message";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn("<html>email content</html>");

        emailService.sendConfirmationEmail(to, username, EmailTemplateName.ACTIVATE_ACCOUNT, confirmationUrl, subject);

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        var contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq(templateName), contextCaptor.capture());

        var capturedContext = contextCaptor.getValue();
        var variableNames = capturedContext.getVariableNames();

        assertTrue(variableNames.contains("username"));
        assertTrue(variableNames.contains("confirmationUrl"));

        assertEquals(username, capturedContext.getVariable("username"));
        assertEquals(confirmationUrl, capturedContext.getVariable("confirmationUrl"));
    }

    @Test
    void shouldSendEmailWithCorrectTemplatePasswordResetEmail() throws MessagingException {
        String to = "test@example.com";
        String username = "JohnDoe";
        String resetUrl = "http://test.com/reset";
        String subject = "Reset your password";
        String templateName = "password_reset_message";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn("<html>email content</html>");

        emailService.sendPasswordResetEmail(to, username, EmailTemplateName.PASSWORD_RESET, resetUrl, subject);

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        var contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq(templateName), contextCaptor.capture());

        var capturedContext = contextCaptor.getValue();
        var variableNames = capturedContext.getVariableNames();

        assertTrue(variableNames.contains("username"));
        assertTrue(variableNames.contains("resetUrl"));

        assertEquals(username, capturedContext.getVariable("username"));
        assertEquals(resetUrl, capturedContext.getVariable("resetUrl"));
    }

    @Test
    void shouldUseDefaultTemplateWhenTemplateIsNullConfirmationEmail() throws MessagingException {
        String to = "test@example.com";
        String username = "JohnDoe";
        String confirmationUrl = "http://test.com/confirm";
        String subject = "Confirm your email";
        String defaultTemplateName = "activate_account_message";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(defaultTemplateName), any(Context.class))).thenReturn("<html>email content</html>");

        emailService.sendConfirmationEmail(to, username, null, confirmationUrl, subject);

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        var contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq(defaultTemplateName), contextCaptor.capture());

        var capturedContext = contextCaptor.getValue();
        var variableNames = capturedContext.getVariableNames();

        assertTrue(variableNames.contains("username"));
        assertTrue(variableNames.contains("confirmationUrl"));

        assertEquals(username, capturedContext.getVariable("username"));
        assertEquals(confirmationUrl, capturedContext.getVariable("confirmationUrl"));
    }
}
