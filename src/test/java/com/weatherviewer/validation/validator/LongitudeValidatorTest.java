package com.weatherviewer.validation.validator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LongitudeValidatorTest {

    private final LongitudeValidator validator = new LongitudeValidator();

    @Test
    void null_returnsTrue() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void validLongitude_returnsTrue() {
        assertThat(validator.isValid(30.52, null)).isTrue();
    }

    @Test
    void atLowerBoundary_returnsTrue() {
        assertThat(validator.isValid(-180.0, null)).isTrue();
    }

    @Test
    void atUpperBoundary_returnsTrue() {
        assertThat(validator.isValid(180.0, null)).isTrue();
    }

    @Test
    void belowLowerBoundary_returnsFalse() {
        assertThat(validator.isValid(-180.1, null)).isFalse();
    }

    @Test
    void aboveUpperBoundary_returnsFalse() {
        assertThat(validator.isValid(180.1, null)).isFalse();
    }

}
