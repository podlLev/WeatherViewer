package com.weatherviewer.dto.ws;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardUpdateMessageTest {

    @Test
    void chainedSetters_returnSameInstanceAndPersistValues() {
        DashboardLocationWeather locationWeather = new DashboardLocationWeather().setLocationName("Kyiv");

        DashboardUpdateMessage message = new DashboardUpdateMessage()
                .setLocations(List.of(locationWeather))
                .setUnavailableLocationNames(List.of("Lviv"));

        assertThat(message.getLocations()).containsExactly(locationWeather);
        assertThat(message.getUnavailableLocationNames()).containsExactly("Lviv");
    }

    @Test
    void newInstance_hasNullLists() {
        DashboardUpdateMessage message = new DashboardUpdateMessage();

        assertThat(message.getLocations()).isNull();
        assertThat(message.getUnavailableLocationNames()).isNull();
    }

}
