package com.weatherviewer.validation.annotation;

import com.weatherviewer.validation.validator.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a password string meets the app's minimum strength rules
 * (length plus a mix of character types). See {@link PasswordValidator}.
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {

    String message() default "Password must be at least 8 characters long and include a mix of letters, numbers, and symbols.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
