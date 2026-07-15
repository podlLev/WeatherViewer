package com.weatherviewer.validation.annotation;


import com.weatherviewer.validation.validator.LongitudeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a {@code Double}/{@code double} field or parameter is a
 * physically valid longitude (-180 to 180 degrees). See {@link LongitudeValidator}.
 */
@Documented
@Constraint(validatedBy = LongitudeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Longitude {

    String message() default "Longitude must be between -180 and 180";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
