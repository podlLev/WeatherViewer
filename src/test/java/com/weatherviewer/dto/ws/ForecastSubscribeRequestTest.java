package com.weatherviewer.dto.ws;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ForecastSubscribeRequestTest {

    @Test
    void gettersAndSetters_roundTripValues() {
        ForecastSubscribeRequest request = new ForecastSubscribeRequest();
        request.setLat(50.45);
        request.setLon(30.52);

        assertThat(request.getLat()).isEqualTo(50.45);
        assertThat(request.getLon()).isEqualTo(30.52);
    }

    @Test
    void newInstance_hasNullCoordinates() {
        ForecastSubscribeRequest request = new ForecastSubscribeRequest();

        assertThat(request.getLat()).isNull();
        assertThat(request.getLon()).isNull();
    }

}
