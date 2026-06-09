package com.weatherviewer.validation.validator;


import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.validation.annotation.UniqueLocation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueLocationValidator implements ConstraintValidator<UniqueLocation, AddLocationDto> {

    private final LocationService locationService;

    @Override
    public boolean isValid(AddLocationDto addLocationDto, ConstraintValidatorContext context) {
        if (addLocationDto == null || addLocationDto.getName() == null || addLocationDto.getUserId() == null) {
            return false;
        }
        boolean exists = locationService.existsByCoordinatesAndUserId(
                addLocationDto.getLatitude(),
                addLocationDto.getLongitude(),
                addLocationDto.getUserId()
        );

        if (exists) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Location with these coordinates already exists for this user")
                    .addPropertyNode("location")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

}
