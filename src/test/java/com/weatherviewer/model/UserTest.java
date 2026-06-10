package com.weatherviewer.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void getFullName_withFirstAndLastName_shouldReturnBoth() {
        User user = new User()
                .setFirstName("John")
                .setLastName("Doe");

        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void getFullName_withOnlyFirstName_shouldReturnFirstName() {
        User user = new User()
                .setFirstName("John")
                .setLastName(null);

        assertEquals("John", user.getFullName());
    }

    @Test
    void getFullName_withOnlyLastName_shouldReturnLastName() {
        User user = new User()
                .setFirstName(null)
                .setLastName("Doe");

        assertEquals("Doe", user.getFullName());
    }

    @Test
    void getFullName_withBothNull_shouldReturnEmptyString() {
        User user = new User()
                .setFirstName(null)
                .setLastName(null);

        assertEquals("", user.getFullName());
    }

    @Test
    void getFullName_withEmptyFirstName_shouldReturnLastName() {
        User user = new User()
                .setFirstName("")
                .setLastName("Doe");

        assertEquals("Doe", user.getFullName());
    }

    @Test
    void getFullName_withBothEmpty_shouldReturnEmptyString() {
        User user = new User()
                .setFirstName("")
                .setLastName("");

        assertEquals("", user.getFullName());
    }

}
