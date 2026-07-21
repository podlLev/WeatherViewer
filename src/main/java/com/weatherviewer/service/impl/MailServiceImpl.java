package com.weatherviewer.service.impl;

import com.weatherviewer.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * {@link MailService} implementation backed by {@link JavaMailSender}.
 * <p>
 * Emails are plain text on purpose (no external images/styles to fetch,
 * nothing for a mail client to block). A misconfigured or unreachable SMTP
 * server never bubbles up as a 500 to the user — sign-up and password
 * reset both still succeed, they just log a warning that no email went
 * out, so the account/token itself is never lost because of a mail outage.
 */
@Service
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public MailServiceImpl(JavaMailSender mailSender,
                           @Value("${app.mail.from:no-reply@weatherviewer.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendVerificationEmail(String to, String firstName, String verificationLink) {
        String subject = "Verify your WeatherViewer account";
        String body = "Hi " + firstName + ",\n\n"
                + "Thanks for signing up for WeatherViewer! Please confirm your email address by opening the link below:\n\n"
                + verificationLink + "\n\n"
                + "This link expires in 24 hours. If you didn't create this account, you can safely ignore this email.\n\n"
                + "— The WeatherViewer team";

        send(to, subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String to, String firstName, String resetLink) {
        String subject = "Reset your WeatherViewer password";
        String body = "Hi " + firstName + ",\n\n"
                + "We received a request to reset your WeatherViewer password. Open the link below to choose a new one:\n\n"
                + resetLink + "\n\n"
                + "This link expires in 1 hour. If you didn't request this, you can safely ignore this email — "
                + "your password will not be changed.\n\n"
                + "— The WeatherViewer team";

        send(to, subject, body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Sent email '{}' to {}", subject, to);
        } catch (MailException e) {
            log.warn("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
        }
    }

}
