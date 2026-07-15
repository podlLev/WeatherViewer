package com.weatherviewer.exception.notfound;

/**
 * Thrown when a requested {@link com.weatherviewer.model.User} account
 * does not exist.
 */
public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }

}
