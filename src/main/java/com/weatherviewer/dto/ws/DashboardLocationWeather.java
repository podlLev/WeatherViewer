package com.weatherviewer.dto.ws;

import com.weatherviewer.dto.WeatherDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

/** Current weather for one saved location, as pushed to a live dashboard subscriber. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class DashboardLocationWeather {

    private UUID locationId;
    private String locationName;
    private WeatherDto weather;

}
