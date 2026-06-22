package com.weatherviewer.validation.validator;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.service.LocationService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UniqueLocationValidatorTest {

    @Mock
    private LocationService locationService;

    @InjectMocks
    private UniqueLocationValidator validator;

    private ConstraintValidatorContext mockContext() {
        return mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);
    }

    private AddLocationDto validDto() {
        return new AddLocationDto()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUserId(UUID.randomUUID());
    }

    @Test
    void null_dto_returnsFalse() {
        assertThat(validator.isValid(null, mockContext())).isFalse();
    }

    @Test
    void null_name_returnsFalse() {
        AddLocationDto dto = validDto().setName(null);
        assertThat(validator.isValid(dto, mockContext())).isFalse();
    }

    @Test
    void null_userId_returnsFalse() {
        AddLocationDto dto = validDto().setUserId(null);
        assertThat(validator.isValid(dto, mockContext())).isFalse();
    }

    @Test
    void null_latitude_returnsFalse() {
        AddLocationDto dto = validDto().setLatitude(null);
        assertThat(validator.isValid(dto, mockContext())).isFalse();
    }

    @Test
    void null_longitude_returnsFalse() {
        AddLocationDto dto = validDto().setLongitude(null);
        assertThat(validator.isValid(dto, mockContext())).isFalse();
    }

    @Test
    void locationNotExists_returnsTrue() {
        AddLocationDto dto = validDto();
        when(locationService.existsByCoordinatesAndUserId(
                dto.getLatitude(), dto.getLongitude(), dto.getUserId()
        )).thenReturn(false);

        assertThat(validator.isValid(dto, mockContext())).isTrue();
    }

    @Test
    void locationAlreadyExists_returnsFalse() {
        AddLocationDto dto = validDto();
        when(locationService.existsByCoordinatesAndUserId(
                dto.getLatitude(), dto.getLongitude(), dto.getUserId()
        )).thenReturn(true);

        assertThat(validator.isValid(dto, mockContext())).isFalse();
    }

}
