package com.weatherviewer.validation.validator;


import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.validation.annotation.UniqueLocation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Backs {@link UniqueLocation}. Requires name, owner, latitude, and
 * longitude to all be present, then checks (after rounding coordinates to
 * 5 decimal places, matching {@link com.weatherviewer.service.impl.LocationServiceImpl})
 * whether the owner already has a saved location at those coordinates.
 */
@Component
@RequiredArgsConstructor
public class UniqueLocationValidator implements ConstraintValidator<UniqueLocation, AddLocationDto> {

    private final LocationService locationService;

    @Override
    public boolean isValid(AddLocationDto addLocationDto, ConstraintValidatorContext context) {
        if (addLocationDto == null
                || addLocationDto.getName() == null
                || addLocationDto.getUserId() == null
                || addLocationDto.getLatitude() == null
                || addLocationDto.getLongitude() == null) {
            return false;
        }

        double roundedLat = round(addLocationDto.getLatitude());
        double roundedLon = round(addLocationDto.getLongitude());

        boolean exists = locationService.existsByCoordinatesAndUserId(roundedLat, roundedLon, addLocationDto.getUserId());

        if (exists) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Location with these coordinates already exists for this user")
                    .addPropertyNode("location")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    /** Rounds a coordinate to 5 decimal places (~1.1m precision), matching the value actually persisted. */
    private double round(double value) {
        return Math.round(value * 100000.0) / 100000.0;
    }

}
