package com.weatherviewer.websocket;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which connected WebSocket sessions want live weather pushes, and
 * for what (a paginated/sorted dashboard view, or a single forecast-page
 * location).
 * <p>
 * Keyed by STOMP session ID rather than user ID: a user could have the
 * dashboard open in one tab and a forecast page in another, each getting
 * its own independent live feed. A session holds at most one subscription
 * of each kind at a time - registering a new one for a session replaces
 * whatever that session was previously subscribed to.
 * <p>
 * Entries are removed by {@link WeatherSocketEventListener} when the
 * underlying WebSocket session disconnects, so this never accumulates
 * subscriptions for clients that have navigated away or closed the tab.
 */
@Component
public class WeatherSubscriptionRegistry {

    private final Map<String, DashboardSubscription> dashboardSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, ForecastSubscription> forecastSubscriptions = new ConcurrentHashMap<>();

    public void registerDashboard(DashboardSubscription subscription) {
        forecastSubscriptions.remove(subscription.sessionId());
        dashboardSubscriptions.put(subscription.sessionId(), subscription);
    }

    public void registerForecast(ForecastSubscription subscription) {
        dashboardSubscriptions.remove(subscription.sessionId());
        forecastSubscriptions.put(subscription.sessionId(), subscription);
    }

    /** Removes any subscription (dashboard or forecast) held by this session, e.g. on disconnect. */
    public void remove(String sessionId) {
        if (sessionId == null) {
            return;
        }
        dashboardSubscriptions.remove(sessionId);
        forecastSubscriptions.remove(sessionId);
    }

    public Collection<DashboardSubscription> dashboardSubscriptions() {
        return dashboardSubscriptions.values();
    }

    public Collection<ForecastSubscription> forecastSubscriptions() {
        return forecastSubscriptions.values();
    }

}
