package com.rainy.homebudgettracker.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
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

    private final String emailFrom = "confirmation@hbt.com";

    @Async
    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplateName,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {
        String templateName;
        if (emailTemplateName == null) {
            templateName = "confirm-email";
        } else {
            templateName = emailTemplateName.name();
        }

        var mimeMessage = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                "UTF-8"
        );

        Map<String, Object> properties = Map.of(
                "username", username,
                "confirmationUrl", confirmationUrl,
                "activationCode", activationCode
        );

        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom(emailFrom);
        helper.setTo(to);
        helper.setSubject(subject);

        String template = templateEngine.process(templateName, context);

        helper.setText(template, true);

        mailSender.send(mimeMessage);
    }
}
