package com.weatherviewer.exception;

import com.weatherviewer.dto.AddLocationDto;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.assertj.core.api.Assertions.assertThat;

class LocationValidationExceptionTest {

    @Test
    void constructor_setsMessage() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new AddLocationDto(), "addLocation");

        LocationValidationException ex = new LocationValidationException(bindingResult);

        assertThat(ex.getMessage()).isEqualTo("Location validation failed");
    }

    @Test
    void constructor_mapsFieldErrors() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new AddLocationDto(), "addLocation");
        bindingResult.rejectValue("name", "error1", "Name cannot be blank");

        LocationValidationException ex = new LocationValidationException(bindingResult);

        assertThat(ex.getErrors()).hasSize(1);
    }

    @Test
    void constructor_mapsMultipleFieldErrors() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new AddLocationDto(), "addLocation");
        bindingResult.rejectValue("name", "error1", "Name cannot be blank");
        bindingResult.rejectValue("latitude", "error2", "Latitude is required");

        LocationValidationException ex = new LocationValidationException(bindingResult);

        assertThat(ex.getErrors()).hasSize(2);
    }

    @Test
    void constructor_noErrors_returnsEmptyList() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new AddLocationDto(), "addLocation");

        LocationValidationException ex = new LocationValidationException(bindingResult);

        assertThat(ex.getErrors()).isEmpty();
    }

    @Test
    void constructor_preservesFieldNameAndMessage() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new AddLocationDto(), "addLocation");
        bindingResult.rejectValue("name", "error1", "Name cannot be blank");

        LocationValidationException ex = new LocationValidationException(bindingResult);

        FieldError error = ex.getErrors().get(0);
        assertThat(error.field()).isEqualTo("name");
        assertThat(error.message()).isEqualTo("Name cannot be blank");
    }

    @Test
    void isInstanceOf_RuntimeException() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new AddLocationDto(), "addLocation");

        LocationValidationException ex = new LocationValidationException(bindingResult);

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

}