package com.weatherviewer.websocket;

import com.weatherviewer.model.enums.UnitSystem;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ForecastSubscriptionTest {

    @Test
    void accessors_returnConstructorValues() {
        UUID userId = UUID.randomUUID();

        ForecastSubscription subscription = new ForecastSubscription(
                "session-1", userId, "john@example.com", UnitSystem.IMPERIAL, 50.45, 30.52);

        assertThat(subscription.sessionId()).isEqualTo("session-1");
        assertThat(subscription.userId()).isEqualTo(userId);
        assertThat(subscription.username()).isEqualTo("john@example.com");
        assertThat(subscription.units()).isEqualTo(UnitSystem.IMPERIAL);
        assertThat(subscription.latitude()).isEqualTo(50.45);
        assertThat(subscription.longitude()).isEqualTo(30.52);
    }

    @Test
    void equals_sameValues_areEqual() {
        UUID userId = UUID.randomUUID();

        ForecastSubscription first = new ForecastSubscription("session-1", userId, "john@example.com", UnitSystem.METRIC, 50.45, 30.52);
        ForecastSubscription second = new ForecastSubscription("session-1", userId, "john@example.com", UnitSystem.METRIC, 50.45, 30.52);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void equals_differentCoordinates_areNotEqual() {
        UUID userId = UUID.randomUUID();

        ForecastSubscription first = new ForecastSubscription("session-1", userId, "john@example.com", UnitSystem.METRIC, 50.45, 30.52);
        ForecastSubscription second = new ForecastSubscription("session-1", userId, "john@example.com", UnitSystem.METRIC, 51.0, 30.52);

        assertThat(first).isNotEqualTo(second);
    }

}
