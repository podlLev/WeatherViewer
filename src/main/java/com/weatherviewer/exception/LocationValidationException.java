package com.weatherviewer.exception;

import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.List;

@Getter
public class LocationValidationException extends RuntimeException {

    private final List<FieldError> errors;

    public LocationValidationException(BindingResult bindingResult) {
        super("Location validation failed");
        this.errors = bindingResult.getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
    }

}
