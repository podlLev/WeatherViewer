package com.weatherviewer.exception;

/**
 * Thrown when an email-verification or password-reset token is missing,
 * of the wrong {@link com.weatherviewer.model.enums.TokenType}, already
 * used, or expired.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

}
