package com.weatherviewer.exception.notfound;

/**
 * Thrown when a requested {@link com.weatherviewer.model.Location} does not
 * exist or does not belong to the requesting user.
 */
public class LocationNotFoundException extends NotFoundException {

    public LocationNotFoundException(String message) {
        super(message);
    }

}
