package com.weatherviewer.model.enums;

/**
 * The purpose of a {@link com.weatherviewer.model.VerificationToken}.
 * <p>
 * Both email verification and password reset use the same underlying
 * token table; this enum keeps the two purposes from being interchangeable
 * (a leaked verification link can never be used to reset a password, and
 * vice versa).
 */
public enum TokenType {

    /** Confirms a newly registered account's email address. */
    EMAIL_VERIFICATION,
    /** Authorizes a one-time password reset. */
    PASSWORD_RESET

}
