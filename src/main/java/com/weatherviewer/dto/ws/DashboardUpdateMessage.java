package com.weatherviewer.dto.ws;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * One live-update tick for a dashboard subscriber: weather for every
 * location on their currently-viewed page, plus the names of any locations
 * whose fetch failed this tick (mirrors {@code HomeController}'s
 * {@code errorMessages} model attribute, so the client can render the same
 * "temporarily unavailable" state it would get from a full page reload).
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class DashboardUpdateMessage {

    private List<DashboardLocationWeather> locations;
    private List<String> unavailableLocationNames;

}
