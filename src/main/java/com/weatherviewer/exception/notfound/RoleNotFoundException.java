package com.weatherviewer.exception.notfound;

/**
 * Thrown when a role name cannot be matched to a known
 * {@link com.weatherviewer.model.enums.Role}.
 */
public class RoleNotFoundException extends NotFoundException {

    public RoleNotFoundException(String message) {
        super(message);
    }

}
