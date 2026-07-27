package com.weatherviewer.event;

/**
 * Published by {@link com.weatherviewer.service.impl.VerificationServiceImpl}
 * once a verification token has been persisted. Consumed by
 * {@link com.weatherviewer.service.impl.MailEventListener}, which sends the
 * actual email asynchronously and only after the surrounding transaction
 * commits — so a rolled-back sign-up never results in an email pointing at
 * a token that was never saved.
 */
public record VerificationEmailRequestedEvent(String email, String firstName, String verificationLink) {
}
