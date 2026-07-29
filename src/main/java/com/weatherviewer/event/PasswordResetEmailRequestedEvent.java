package com.weatherviewer.event;

/**
 * Published by {@link com.weatherviewer.service.impl.VerificationServiceImpl}
 * once a password-reset token has been persisted. Consumed by
 * {@link com.weatherviewer.service.impl.MailEventListener}, which sends the
 * actual email asynchronously and only after the surrounding transaction
 * commits — so a rolled-back reset request never results in an email
 * pointing at a token that was never saved.
 */
public record PasswordResetEmailRequestedEvent(String email, String firstName, String resetLink) {
}
