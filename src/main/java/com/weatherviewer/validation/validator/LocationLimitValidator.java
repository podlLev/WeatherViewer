package com.weatherviewer.validation.validator;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.validation.annotation.LocationLimit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Backs {@link LocationLimit}. Counts how many locations the owning user
 * already has saved and rejects the new one if that count is already at
 * or above {@code location.max-per-user}.
 * <p>
 * Skips the check (returns valid) when {@code userId} is missing, since
 * that case is already reported by the DTO's own required-field
 * validation and shouldn't also surface a misleading "limit reached"
 * message.
 */
@Component
@RequiredArgsConstructor
public class LocationLimitValidator implements ConstraintValidator<LocationLimit, AddLocationDto> {

    private final LocationService locationService;

    @Value("${location.max-per-user:100}")
    private int maxLocationsPerUser;

    @Override
    public boolean isValid(AddLocationDto addLocationDto, ConstraintValidatorContext context) {
        if (addLocationDto == null || addLocationDto.getUserId() == null) {
            return true;
        }

        long currentCount = locationService.countByUserId(addLocationDto.getUserId());

        if (currentCount >= maxLocationsPerUser) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "You've reached the maximum of " + maxLocationsPerUser + " saved locations")
                    .addPropertyNode("location")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

}
