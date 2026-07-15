package com.weatherviewer.dto.enums;

import java.time.LocalDateTime;

/**
 * Coarse day/night classification for a weather entry, used to pick the
 * matching day/night icon variant on the frontend.
 */
public enum TimeOfDay {
    DAY,
    NIGHT,
    UNDEFINED;

    /**
     * Classifies a timestamp as day or night using a fixed 08:00–20:00
     * daytime window (not based on actual sunrise/sunset for the location).
     *
     * @param time the local date/time to classify; may be {@code null}
     * @return {@link #DAY} for hours 8–20 inclusive, {@link #NIGHT} otherwise,
     *         or {@link #UNDEFINED} if {@code time} is {@code null}
     */
    public static TimeOfDay getTimeOfDayForTime(LocalDateTime time) {
        if (time == null) {
            return UNDEFINED;
        }
        int hour = time.getHour();
        return hour >= 8 && hour <= 20 ? DAY : NIGHT;
    }

}
