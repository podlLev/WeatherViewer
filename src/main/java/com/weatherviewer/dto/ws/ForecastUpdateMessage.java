package com.weatherviewer.dto.ws;

import com.weatherviewer.dto.WeatherDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/** One live-update tick for a forecast-page subscriber: refreshed hourly and daily forecast entries. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ForecastUpdateMessage {

    private List<WeatherDto> hourlyForecast;
    private List<WeatherDto> dailyForecast;

}
