package com.weatherviewer.exception;

import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.List;

/**
 * Thrown by {@link com.weatherviewer.rest.LocationController} when a
 * manually-invoked {@link org.springframework.validation.Validator} run
 * against an {@link com.weatherviewer.dto.AddLocationDto} (used on the
 * caller-scoped {@code /api/v1/locations/my} endpoint, where the owning
 * user ID is injected before validation) reports errors. Carries the
 * resulting field errors so {@link ControllerExceptionHandler} can return
 * them as a structured {@code 422} response.
 */
@Getter
public class LocationValidationException extends RuntimeException {

    /** Field-level validation failures collected from the binding result. */
    private final List<FieldError> errors;

    /**
     * @param bindingResult the binding result whose field errors should be captured
     */
    public LocationValidationException(BindingResult bindingResult) {
        super("Location validation failed");
        this.errors = bindingResult.getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
    }

}
