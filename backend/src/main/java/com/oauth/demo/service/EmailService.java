package com.oauth.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    public void sendEmail(String email, String subject, String content)
            throws MessagingException, UnsupportedEncodingException {
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
        System.out.println("✅ EMAIL SENT SUCCESSFULLY");
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
            System.out.println("❌ EMAIL FAILED");
            e.printStackTrace();
        }
    }
}