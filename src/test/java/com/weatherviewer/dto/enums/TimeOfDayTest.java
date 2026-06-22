package com.weatherviewer.dto.enums;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TimeOfDayTest {

    @Test
    void null_returnsUndefined() {
        assertThat(TimeOfDay.getTimeOfDayForTime(null)).isEqualTo(TimeOfDay.UNDEFINED);
    }

    @Test
    void hour_8_returnsDay() {
        LocalDateTime time = LocalDateTime.now().withHour(8);
        assertThat(TimeOfDay.getTimeOfDayForTime(time)).isEqualTo(TimeOfDay.DAY);
    }

    @Test
    void hour_20_returnsDay() {
        LocalDateTime time = LocalDateTime.now().withHour(20);
        assertThat(TimeOfDay.getTimeOfDayForTime(time)).isEqualTo(TimeOfDay.DAY);
    }

    @Test
    void hour_14_returnsDay() {
        LocalDateTime time = LocalDateTime.now().withHour(14);
        assertThat(TimeOfDay.getTimeOfDayForTime(time)).isEqualTo(TimeOfDay.DAY);
    }

    @Test
    void hour_7_returnsNight() {
        LocalDateTime time = LocalDateTime.now().withHour(7);
        assertThat(TimeOfDay.getTimeOfDayForTime(time)).isEqualTo(TimeOfDay.NIGHT);
    }

    @Test
    void hour_21_returnsNight() {
        LocalDateTime time = LocalDateTime.now().withHour(21);
        assertThat(TimeOfDay.getTimeOfDayForTime(time)).isEqualTo(TimeOfDay.NIGHT);
    }

    @Test
    void hour_0_returnsNight() {
        LocalDateTime time = LocalDateTime.now().withHour(0);
        assertThat(TimeOfDay.getTimeOfDayForTime(time)).isEqualTo(TimeOfDay.NIGHT);
    }

    @Test
    void hour_23_returnsNight() {
        LocalDateTime time = LocalDateTime.now().withHour(23);
        assertThat(TimeOfDay.getTimeOfDayForTime(time)).isEqualTo(TimeOfDay.NIGHT);
    }

}
