package com.weatherviewer.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldErrorTest {

    @Test
    void constructor_setsFieldAndMessage() {
        FieldError error = new FieldError("email", "Invalid email format");
        assertThat(error.field()).isEqualTo("email");
        assertThat(error.message()).isEqualTo("Invalid email format");
    }

    @Test
    void equalRecords_areEqual() {
        FieldError error1 = new FieldError("email", "Invalid email format");
        FieldError error2 = new FieldError("email", "Invalid email format");
        assertThat(error1).isEqualTo(error2);
    }

    @Test
    void differentRecords_areNotEqual() {
        FieldError error1 = new FieldError("email", "Invalid email format");
        FieldError error2 = new FieldError("name", "Name cannot be blank");
        assertThat(error1).isNotEqualTo(error2);
    }

    @Test
    void toString_containsFieldAndMessage() {
        FieldError error = new FieldError("email", "Invalid email format");
        assertThat(error.toString())
                .contains("email")
                .contains("Invalid email format");
    }

}
