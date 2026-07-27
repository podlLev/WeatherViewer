package com.weatherviewer.service.impl;

import com.weatherviewer.event.PasswordResetEmailRequestedEvent;
import com.weatherviewer.event.VerificationEmailRequestedEvent;
import com.weatherviewer.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges {@code *EmailRequestedEvent}s raised by
 * {@link VerificationServiceImpl} to actual sends via {@link MailService}.
 * <p>
 * Two things happen here that matter for reliability:
 * <ul>
 *   <li>{@link TransactionalEventListener} with
 *       {@link TransactionPhase#AFTER_COMMIT} defers the send until the
 *       transaction that created the token has actually committed. If that
 *       transaction rolls back for any reason, the event is discarded and
 *       no email goes out — previously, the mail call sat inside the same
 *       {@code @Transactional} method as the token write, so a caller could
 *       (in principle) receive a link for a token that was never persisted.</li>
 *   <li>{@link Async} runs the send on a dedicated pool
 *       ({@code mailTaskExecutor}, see {@code AppConfig}) rather than the
 *       request thread, so a slow or unreachable SMTP server can no longer
 *       add latency to sign-up, email verification, or password-reset
 *       requests.</li>
 * </ul>
 * {@link MailService} itself still retries transient SMTP failures and
 * never throws, so a failure here is logged by it directly and does not
 * propagate any further.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MailEventListener {

    private final MailService mailService;

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificationEmailRequested(VerificationEmailRequestedEvent event) {
        log.debug("Dispatching verification email to {}", event.email());
        mailService.sendVerificationEmail(event.email(), event.firstName(), event.verificationLink());
    }

    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetEmailRequested(PasswordResetEmailRequestedEvent event) {
        log.debug("Dispatching password reset email to {}", event.email());
        mailService.sendPasswordResetEmail(event.email(), event.firstName(), event.resetLink());
    }

}
