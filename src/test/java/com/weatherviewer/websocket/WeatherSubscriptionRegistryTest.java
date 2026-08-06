package com.weatherviewer.websocket;

import com.weatherviewer.model.enums.UnitSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherSubscriptionRegistryTest {

    private WeatherSubscriptionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new WeatherSubscriptionRegistry();
    }

    private DashboardSubscription dashboardSubscription(String sessionId) {
        return new DashboardSubscription(sessionId, UUID.randomUUID(), "john@example.com", UnitSystem.METRIC, "date", 0);
    }

    private ForecastSubscription forecastSubscription(String sessionId) {
        return new ForecastSubscription(sessionId, UUID.randomUUID(), "john@example.com", UnitSystem.METRIC, 50.45, 30.52);
    }

    @Test
    void registerDashboard_addsSubscription() {
        DashboardSubscription subscription = dashboardSubscription("session-1");

        registry.registerDashboard(subscription);

        assertThat(registry.dashboardSubscriptions()).containsExactly(subscription);
        assertThat(registry.forecastSubscriptions()).isEmpty();
    }

    @Test
    void registerForecast_addsSubscription() {
        ForecastSubscription subscription = forecastSubscription("session-1");

        registry.registerForecast(subscription);

        assertThat(registry.forecastSubscriptions()).containsExactly(subscription);
        assertThat(registry.dashboardSubscriptions()).isEmpty();
    }

    @Test
    void registerDashboard_sameSessionTwice_replacesPreviousSubscription() {
        registry.registerDashboard(dashboardSubscription("session-1"));
        DashboardSubscription replacement = new DashboardSubscription("session-1", UUID.randomUUID(), "john@example.com", UnitSystem.METRIC, "nameAsc", 1);

        registry.registerDashboard(replacement);

        assertThat(registry.dashboardSubscriptions()).containsExactly(replacement);
    }

    @Test
    void registerDashboard_sessionHadForecastSubscription_removesForecastSubscription() {
        registry.registerForecast(forecastSubscription("session-1"));

        registry.registerDashboard(dashboardSubscription("session-1"));

        assertThat(registry.forecastSubscriptions()).isEmpty();
        assertThat(registry.dashboardSubscriptions()).hasSize(1);
    }

    @Test
    void registerForecast_sessionHadDashboardSubscription_removesDashboardSubscription() {
        registry.registerDashboard(dashboardSubscription("session-1"));

        registry.registerForecast(forecastSubscription("session-1"));

        assertThat(registry.dashboardSubscriptions()).isEmpty();
        assertThat(registry.forecastSubscriptions()).hasSize(1);
    }

    @Test
    void remove_removesDashboardSubscription() {
        registry.registerDashboard(dashboardSubscription("session-1"));

        registry.remove("session-1");

        assertThat(registry.dashboardSubscriptions()).isEmpty();
    }

    @Test
    void remove_removesForecastSubscription() {
        registry.registerForecast(forecastSubscription("session-1"));

        registry.remove("session-1");

        assertThat(registry.forecastSubscriptions()).isEmpty();
    }

    @Test
    void remove_unknownSessionId_doesNothing() {
        registry.registerDashboard(dashboardSubscription("session-1"));

        registry.remove("session-unknown");

        assertThat(registry.dashboardSubscriptions()).hasSize(1);
    }

    @Test
    void remove_nullSessionId_doesNotThrow() {
        registry.registerDashboard(dashboardSubscription("session-1"));

        registry.remove(null);

        assertThat(registry.dashboardSubscriptions()).hasSize(1);
    }

    @Test
    void multipleDifferentSessions_areTrackedIndependently() {
        DashboardSubscription first = dashboardSubscription("session-1");
        DashboardSubscription second = dashboardSubscription("session-2");

        registry.registerDashboard(first);
        registry.registerDashboard(second);

        assertThat(registry.dashboardSubscriptions()).containsExactlyInAnyOrder(first, second);
    }

}
