package com.weatherviewer.validation.validator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LatitudeValidatorTest {

    private final LatitudeValidator validator = new LatitudeValidator();

    @Test
    void null_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void validLatitude_returnsTrue() {
        assertThat(validator.isValid(50.45, null)).isTrue();
    }

    @Test
    void atLowerBoundary_returnsTrue() {
        assertThat(validator.isValid(-90.0, null)).isTrue();
    }

    @Test
    void atUpperBoundary_returnsTrue() {
        assertThat(validator.isValid(90.0, null)).isTrue();
    }

    @Test
    void belowLowerBoundary_returnsFalse() {
        assertThat(validator.isValid(-90.1, null)).isFalse();
    }

    @Test
    void aboveUpperBoundary_returnsFalse() {
        assertThat(validator.isValid(90.1, null)).isFalse();
    }

}
