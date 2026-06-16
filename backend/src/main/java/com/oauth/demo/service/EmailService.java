package com.oauth.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendEmail(String email, String subject, String content)
            throws MessagingException, UnsupportedEncodingException {
        if (mailSender == null) {
            log.debug("Mail sender not configured — skipping email to {}", email);
            return;
        }
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email is null!");
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("no-reply@editorplatform.com", "editorplatform");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
        log.info("Verification email sent to {}", email);
    }

    public void sendVerificationEmail(String email, String token) {
        String subject = "Verify your email";
        String content =
                "<h3>Email Verification</h3>" +
                        "<p>Your verification code is:</p>" +
                        "<h2>" + token + "</h2>";

        try {
            sendEmail(email, subject, content);
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}: {}", email, e.getMessage());
        }
    }
}
