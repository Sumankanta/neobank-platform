package com.infy.NeoBank.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String text) {
        log.info("Preparing to send email to: {}, Subject: {}", to, subject);
        log.info("Email Body Content:\n----------------------------------------\n{}\n----------------------------------------", text);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("no-reply@neobank.com");
            mailSender.send(message);
            log.info("Email successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {} via SMTP: {}. (This is normal in local/dev environments without a configured SMTP server. The email text was printed to logs above.)", to, e.getMessage());
        }
    }
}
