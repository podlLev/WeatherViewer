package com.weatherviewer.validation.annotation;

import com.weatherviewer.validation.validator.UniqueLocationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Class-level constraint on {@link com.weatherviewer.dto.AddLocationDto}
 * verifying the owning user hasn't already saved a location with the same
 * name or the same coordinates. See {@link UniqueLocationValidator}.
 */
@Documented
@Constraint(validatedBy = UniqueLocationValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueLocation {

    String message() default "Location already exists for this user";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
