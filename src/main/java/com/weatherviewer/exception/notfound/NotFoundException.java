package com.weatherviewer.exception.notfound;

/**
 * Base type for all "resource not found" exceptions. Handled globally to
 * produce a 404 response (REST) or a redirect with an error message (MVC).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

}
