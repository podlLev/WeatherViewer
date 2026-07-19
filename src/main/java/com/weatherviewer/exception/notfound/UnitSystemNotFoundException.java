package com.weatherviewer.exception.notfound;

/**
 * Thrown when a unit system name cannot be matched to a known
 * {@link com.weatherviewer.model.enums.UnitSystem}.
 */
public class UnitSystemNotFoundException extends NotFoundException {

    public UnitSystemNotFoundException(String message) {
        super(message);
    }

}
