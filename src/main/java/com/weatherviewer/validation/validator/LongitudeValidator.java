package com.weatherviewer.validation.validator;

import com.weatherviewer.validation.annotation.Longitude;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Backs {@link Longitude}: valid when {@code null} or within [-180, 180]. */
public class LongitudeValidator implements ConstraintValidator<Longitude, Double> {

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        return value == null || value >= -180.0 && value <= 180.0;
    }

}
