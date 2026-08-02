package com.weatherviewer.dto.ws;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Sent by the client (STOMP SEND to {@code /app/forecast.subscribe}) after connecting to the forecast page. */
@Getter
@Setter
@NoArgsConstructor
public class ForecastSubscribeRequest {

    private Double lat;
    private Double lon;

}
