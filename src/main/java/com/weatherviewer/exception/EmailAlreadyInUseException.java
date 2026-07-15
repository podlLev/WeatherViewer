package com.weatherviewer.exception;

/**
 * Thrown when an operation would register or assign an email address that
 * is already associated with another {@link com.weatherviewer.model.User}
 * account. Also raised (indirectly, via Bean Validation) by
 * {@link com.weatherviewer.validation.validator.UniqueEmailValidator}.
 */
public class EmailAlreadyInUseException extends RuntimeException {

    /**
     * @param email the email address that is already in use
     */
    public EmailAlreadyInUseException(String email) {
        super("Email already in use: " + email);
    }

}
