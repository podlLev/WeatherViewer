package com.weatherviewer.service;

/**
 * Outbound transactional email. Implementations must never let a mail
 * provider outage break the calling request — failures are retried a few
 * times, then logged and swallowed rather than propagated.
 * <p>
 * In practice these methods are only ever invoked by
 * {@link com.weatherviewer.service.impl.MailEventListener}, asynchronously
 * and after the transaction that created the underlying token has
 * committed (see {@link com.weatherviewer.event.VerificationEmailRequestedEvent}
 * / {@link com.weatherviewer.event.PasswordResetEmailRequestedEvent}), so a
 * slow or unreachable mail server never adds latency to — or breaks — the
 * sign-up, verification, or password-reset request itself.
 */
public interface MailService {

    /** Sends a new-account email containing a link to confirm the given address. */
    void sendVerificationEmail(String to, String firstName, String verificationLink);

    /** Sends a link allowing the recipient to set a new password. */
    void sendPasswordResetEmail(String to, String firstName, String resetLink);

}
