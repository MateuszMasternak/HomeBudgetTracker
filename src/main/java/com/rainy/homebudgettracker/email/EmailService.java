package com.rainy.homebudgettracker.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Async
    public void sendConfirmationEmail(
            String to,
            String username,
            EmailTemplateName emailTemplateName,
            String confirmationUrl,
            String subject
    ) throws MessagingException {
        String templateName;
        if (emailTemplateName == null) {
            templateName = "activate_account_message";
        } else {
            templateName = emailTemplateName.getName();
        }

        var mimeMessage = mailSender.createMimeMessage();
        var helper = createMimeMessageHelper(mimeMessage, to, subject);

        Map<String, Object> properties = Map.of(
                "username", username,
                "confirmationUrl", confirmationUrl
        );

        Context context = new Context();
        context.setVariables(properties);

        String template = templateEngine.process(templateName, context);

        helper.setText(template, true);

        mailSender.send(mimeMessage);
    }

    @Async
    public void sendPasswordResetEmail(
            String to,
            String username,
            EmailTemplateName emailTemplateName,
            String resetUrl,
            String subject
    ) throws MessagingException {
        String templateName;
        if (emailTemplateName == null) {
            templateName = "password_reset_message";
        } else {
            templateName = emailTemplateName.getName();
        }

        Map<String, Object> properties = Map.of(
                "username", username,
                "resetUrl", resetUrl
        );

        var mimeMessage = mailSender.createMimeMessage();
        var helper = createMimeMessageHelper(mimeMessage, to, subject);

        Context context = new Context();
        context.setVariables(properties);

        String template = templateEngine.process(templateName, context);

        helper.setText(template, true);

        mailSender.send(mimeMessage);
    }

    private MimeMessageHelper createMimeMessageHelper(MimeMessage mimeMessage, String to, String subject) throws MessagingException {
        var helper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                "UTF-8"
        );
        helper.setPriority(1);
        helper.setFrom(emailFrom);
        helper.setTo(to);
        helper.setSubject(subject);

        return helper;
    }
}
