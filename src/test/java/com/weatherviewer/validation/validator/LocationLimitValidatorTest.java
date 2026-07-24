package com.weatherviewer.validation.validator;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.service.LocationService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationLimitValidatorTest {

    private static final int MAX_LOCATIONS = 100;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationLimitValidator validator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(validator, "maxLocationsPerUser", MAX_LOCATIONS);
    }

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
    void null_dto_returnsTrue() {
        assertThat(validator.isValid(null, mockContext())).isTrue();
    }

    @Test
    void null_userId_returnsTrue() {
        AddLocationDto dto = validDto().setUserId(null);
        assertThat(validator.isValid(dto, mockContext())).isTrue();
    }

    @Test
    void underLimit_returnsTrue() {
        AddLocationDto dto = validDto();
        when(locationService.countByUserId(dto.getUserId())).thenReturn((long) (MAX_LOCATIONS - 1));

        assertThat(validator.isValid(dto, mockContext())).isTrue();
    }

    @Test
    void atLimit_returnsFalse() {
        AddLocationDto dto = validDto();
        when(locationService.countByUserId(dto.getUserId())).thenReturn((long) MAX_LOCATIONS);

        assertThat(validator.isValid(dto, mockContext())).isFalse();
    }

    @Test
    void overLimit_returnsFalse() {
        AddLocationDto dto = validDto();
        when(locationService.countByUserId(dto.getUserId())).thenReturn((long) (MAX_LOCATIONS + 1));

        assertThat(validator.isValid(dto, mockContext())).isFalse();
    }

}
