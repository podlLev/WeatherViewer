package com.weatherviewer.validation.annotation;

import com.weatherviewer.validation.validator.LocationLimitValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Class-level constraint on {@link com.weatherviewer.dto.AddLocationDto}
 * rejecting a new location once the owning user has reached the
 * configured per-user cap ({@code location.max-per-user}, default 100).
 * <p>
 * Without this, a single account (or a script hitting
 * {@code /api/v1/locations/my} directly) could save an unbounded number
 * of locations, and every dashboard load fans out one weather call per
 * saved location. See {@link LocationLimitValidator}.
 */
@Documented
@Constraint(validatedBy = LocationLimitValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocationLimit {

    String message() default "Maximum number of saved locations reached";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
