package com.weatherviewer.validation.validator;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.validation.annotation.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Backs {@link PasswordMatches}. Rejects a {@code null} DTO outright; when
 * {@code password} and {@code repeatPassword} differ, attaches the
 * violation to the {@code repeatPassword} field specifically (rather than
 * the class-level default) so it surfaces next to the right form field.
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, CreateUserDto> {

    @Override
    public boolean isValid(CreateUserDto dto, ConstraintValidatorContext context) {
        if (dto == null) return false;

        if (!Objects.equals(dto.getPassword(), dto.getRepeatPassword())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Passwords do not match")
                    .addPropertyNode("repeatPassword")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

}
