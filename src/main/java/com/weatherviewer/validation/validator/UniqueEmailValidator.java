package com.weatherviewer.validation.validator;

import com.weatherviewer.repository.UserRepository;
import com.weatherviewer.validation.annotation.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Backs {@link UniqueEmail}, checking the database for an existing account
 * with the given email. A {@code null}/blank value is treated as valid
 * here — required-ness is enforced separately via {@code @NotBlank}.
 * <p>
 * Note: does not exempt the current user's own (unchanged) email, so this
 * validator should only be applied where that distinction doesn't matter
 * (e.g. registration); update flows check uniqueness manually instead.
 */
@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) return true;
        return !userRepository.existsByEmail(email);
    }

}
