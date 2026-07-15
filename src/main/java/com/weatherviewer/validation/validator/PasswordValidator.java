package com.weatherviewer.validation.validator;

import com.weatherviewer.validation.annotation.Password;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Backs {@link Password}. Requires at least 8 non-whitespace characters
 * containing a digit, a lowercase letter, an uppercase letter, and a
 * symbol from {@code @#$%^&+=}. A {@code null}/blank value is treated as
 * valid here — required-ness is enforced separately via {@code @NotBlank}.
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) return true;
        return password.matches(PASSWORD_PATTERN);
    }

}
