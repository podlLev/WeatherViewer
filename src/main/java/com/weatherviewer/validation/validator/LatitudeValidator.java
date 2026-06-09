package com.weatherviewer.validation.validator;

import com.weatherviewer.validation.annotation.Latitude;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LatitudeValidator implements ConstraintValidator<Latitude, Double> {

    @Override
    public boolean isValid(Double value, ConstraintValidatorContext context) {
        return value >= -90.0 && value <= 90.0;
    }

}
