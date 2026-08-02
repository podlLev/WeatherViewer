package com.weatherviewer.websocket;

import com.weatherviewer.model.enums.UnitSystem;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardSubscriptionTest {

    @Test
    void accessors_returnConstructorValues() {
        UUID userId = UUID.randomUUID();

        DashboardSubscription subscription = new DashboardSubscription(
                "session-1", userId, "john@example.com", UnitSystem.METRIC, "nameAsc", 2);

        assertThat(subscription.sessionId()).isEqualTo("session-1");
        assertThat(subscription.userId()).isEqualTo(userId);
        assertThat(subscription.username()).isEqualTo("john@example.com");
        assertThat(subscription.units()).isEqualTo(UnitSystem.METRIC);
        assertThat(subscription.sort()).isEqualTo("nameAsc");
        assertThat(subscription.page()).isEqualTo(2);
    }

    @Test
    void equals_sameValues_areEqual() {
        UUID userId = UUID.randomUUID();

        DashboardSubscription first = new DashboardSubscription("session-1", userId, "john@example.com", UnitSystem.METRIC, "date", 0);
        DashboardSubscription second = new DashboardSubscription("session-1", userId, "john@example.com", UnitSystem.METRIC, "date", 0);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    void equals_differentSessionId_areNotEqual() {
        UUID userId = UUID.randomUUID();

        DashboardSubscription first = new DashboardSubscription("session-1", userId, "john@example.com", UnitSystem.METRIC, "date", 0);
        DashboardSubscription second = new DashboardSubscription("session-2", userId, "john@example.com", UnitSystem.METRIC, "date", 0);

        assertThat(first).isNotEqualTo(second);
    }

}
