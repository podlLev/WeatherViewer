package com.weatherviewer.validation.validator;

import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.validation.annotation.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
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
