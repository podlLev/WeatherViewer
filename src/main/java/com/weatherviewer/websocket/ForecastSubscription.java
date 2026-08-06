package com.weatherviewer.websocket;

import com.weatherviewer.model.enums.UnitSystem;

import java.util.UUID;

/**
 * One client's live-forecast subscription: which user, in which unit
 * system, watching the hourly/daily forecast for a single coordinate pair.
 * Mirrors the parameters {@code ForecastController#getForecast} takes from
 * the query string.
 *
 * @param sessionId the STOMP session that registered this subscription
 * @param userId    owner of the forecast page (used to re-verify the location is still theirs)
 * @param username  owner's username (email) - the STOMP user-destination principal name
 * @param units     owner's preferred display units, applied to pushed weather
 * @param latitude  location latitude
 * @param longitude location longitude
 */
public record ForecastSubscription(String sessionId, UUID userId,
                                   String username, UnitSystem units,
                                   double latitude, double longitude) {
}
