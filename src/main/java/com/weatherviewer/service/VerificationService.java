package com.weatherviewer.service;

import com.weatherviewer.model.User;

/**
 * Issues and redeems the one-time tokens behind email verification and
 * password reset.
 */
public interface VerificationService {

    /** Generates a fresh email-verification token for {@code user} and emails it to them. */
    void sendVerificationEmail(User user);

    /**
     * Redeems an email-verification token: activates the owning account and
     * consumes the token.
     *
     * @throws com.weatherviewer.exception.InvalidTokenException if the token is unknown, of the wrong type, expired, or already used
     */
    void confirmEmail(String token);

    /**
     * If {@code email} belongs to an account, issues a password-reset token
     * and emails it. Does nothing (and never reveals whether the address is
     * registered) otherwise, to avoid leaking which emails have accounts.
     */
    void requestPasswordReset(String email);

    /**
     * Redeems a password-reset token, setting the owning account's password
     * to {@code newRawPassword} and consuming the token.
     *
     * @throws com.weatherviewer.exception.InvalidTokenException if the token is unknown, of the wrong type, expired, or already used
     */
    void resetPassword(String token, String newRawPassword);

}
