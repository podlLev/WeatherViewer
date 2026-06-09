package com.weatherviewer.validation.annotation;

import com.weatherviewer.validation.validator.UniqueLocationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueLocationValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueLocation {

    String message() default "Location already exists for this user";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
