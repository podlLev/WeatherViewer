package com.weatherviewer.service.impl;

import com.weatherviewer.service.MailService;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
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
 * nothing for a mail client to block). Transient SMTP failures are retried
 * via the {@code mail} Resilience4j retry instance (same pattern as the
 * {@code weatherApi} retry around the OpenWeatherMap client) before giving
 * up; a misconfigured or unreachable SMTP server never bubbles up past this
 * class. Retry is applied programmatically here, rather than via
 * {@code @Retry}, because the send happens on a private helper called from
 * within this same bean — an annotation-based retry would be silently
 * skipped by Spring AOP's proxy on that kind of self-invocation.
 * <p>
 * Sending itself is now invoked off the request thread: {@link MailService}
 * is only ever called by {@link MailEventListener}, asynchronously, after
 * the transaction that created the underlying token has committed. That
 * keeps a mail outage from adding latency to (or breaking) sign-up,
 * verification, or password-reset requests.
 */
@Service
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final Retry retry;
    private final String fromAddress;

    public MailServiceImpl(JavaMailSender mailSender,
                           RetryRegistry retryRegistry,
                           @Value("${app.mail.from:no-reply@weatherviewer.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.retry = retryRegistry.retry("mail");
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

    /**
     * Sends the message, retrying transient SMTP failures via the {@code mail}
     * Resilience4j retry instance. If every attempt fails, the failure is
     * logged and swallowed here rather than propagated — callers (in
     * practice, {@link MailEventListener} running on its own thread) never
     * need to handle a mail-specific exception.
     */
    private void send(String to, String subject, String body) {
        try {
            retry.executeRunnable(() -> {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromAddress);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
            });
            log.info("Sent email '{}' to {}", subject, to);
        } catch (MailException e) {
            log.warn("Failed to send email '{}' to {} after retries: {}", subject, to, e.getMessage());
        }
    }

}
