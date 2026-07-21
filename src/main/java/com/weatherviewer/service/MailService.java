package com.weatherviewer.service;

/**
 * Outbound transactional email. Implementations must never let a mail
 * provider outage break the calling request (sign-up, password reset) —
 * failures are logged and swallowed rather than propagated.
 */
public interface MailService {

    /** Sends a new-account email containing a link to confirm the given address. */
    void sendVerificationEmail(String to, String firstName, String verificationLink);

    /** Sends a link allowing the recipient to set a new password. */
    void sendPasswordResetEmail(String to, String firstName, String resetLink);

}
