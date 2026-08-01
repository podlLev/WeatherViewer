package com.weatherviewer.websocket;

import com.weatherviewer.model.enums.UnitSystem;

import java.util.UUID;

/**
 * One client's live-dashboard subscription: which user, in which unit
 * system, viewing which sorted/paginated slice of their saved locations.
 * Mirrors the parameters {@code HomeController#home} takes from the query
 * string, so the scheduler can recompute exactly the same page.
 *
 * @param sessionId the STOMP session that registered this subscription
 * @param userId    owner of the dashboard
 * @param username  owner's username (email) - the STOMP user-destination principal name
 * @param units     owner's preferred display units, applied to pushed weather
 * @param sort      dashboard sort key ({@code date}, {@code nameAsc}, {@code nameDesc}, {@code favoriteFirst}, {@code favoritesOnly})
 * @param page      0-based dashboard page number
 */
public record DashboardSubscription(String sessionId, UUID userId,
                                    String username, UnitSystem units,
                                    String sort, int page) {
}
