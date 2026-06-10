package com.weatherviewer.dto.enums;

import java.time.LocalDateTime;

public enum TimeOfDay {
    DAY,
    NIGHT,
    UNDEFINED;

    public static TimeOfDay getTimeOfDayForTime(LocalDateTime time) {
        if (time == null) {
            return UNDEFINED;
        }
        int hour = time.getHour();
        return hour >= 8 && hour <= 20 ? DAY : NIGHT;
    }

}
