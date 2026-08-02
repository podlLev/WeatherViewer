package com.weatherviewer.websocket;

import com.weatherviewer.dto.ws.DashboardSubscribeRequest;
import com.weatherviewer.dto.ws.ForecastSubscribeRequest;
import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.security.SecUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherSocketControllerTest {

    @Mock
    private WeatherSubscriptionRegistry registry;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    private WeatherSocketController controller;

    @BeforeEach
    void setUp() {
        controller = new WeatherSocketController(registry);
    }

    private SecUser secUser() {
        return new SecUser(
                UUID.randomUUID(),
                "john@example.com",
                "hashed",
                Set.of(),
                true,
                "John Doe",
                UnitSystem.METRIC,
                null
        );
    }

    private Authentication authenticationFor(SecUser user) {
        return new TestingAuthenticationToken(user, null);
    }

    @Test
    void subscribeDashboard_validRequest_registersSubscription() {
        SecUser user = secUser();
        Authentication principal = authenticationFor(user);
        when(headerAccessor.getSessionId()).thenReturn("session-1");

        DashboardSubscribeRequest request = new DashboardSubscribeRequest();
        request.setSort("nameAsc");
        request.setPage(2);

        controller.subscribeDashboard(request, principal, headerAccessor);

        ArgumentCaptor<DashboardSubscription> captor = ArgumentCaptor.forClass(DashboardSubscription.class);
        verify(registry).registerDashboard(captor.capture());
        DashboardSubscription subscription = captor.getValue();
        assertThat(subscription.sessionId()).isEqualTo("session-1");
        assertThat(subscription.userId()).isEqualTo(user.getId());
        assertThat(subscription.username()).isEqualTo("john@example.com");
        assertThat(subscription.units()).isEqualTo(UnitSystem.METRIC);
        assertThat(subscription.sort()).isEqualTo("nameAsc");
        assertThat(subscription.page()).isEqualTo(2);
    }

    @Test
    void subscribeDashboard_nullSort_defaultsToDate() {
        Authentication principal = authenticationFor(secUser());
        when(headerAccessor.getSessionId()).thenReturn("session-1");

        controller.subscribeDashboard(new DashboardSubscribeRequest(), principal, headerAccessor);

        ArgumentCaptor<DashboardSubscription> captor = ArgumentCaptor.forClass(DashboardSubscription.class);
        verify(registry).registerDashboard(captor.capture());
        assertThat(captor.getValue().sort()).isEqualTo("date");
        assertThat(captor.getValue().page()).isEqualTo(0);
    }

    @Test
    void subscribeDashboard_unrecognizedSort_defaultsToDate() {
        Authentication principal = authenticationFor(secUser());
        when(headerAccessor.getSessionId()).thenReturn("session-1");

        DashboardSubscribeRequest request = new DashboardSubscribeRequest();
        request.setSort("not-a-real-sort");

        controller.subscribeDashboard(request, principal, headerAccessor);

        ArgumentCaptor<DashboardSubscription> captor = ArgumentCaptor.forClass(DashboardSubscription.class);
        verify(registry).registerDashboard(captor.capture());
        assertThat(captor.getValue().sort()).isEqualTo("date");
    }

    @Test
    void subscribeDashboard_negativePage_defaultsToZero() {
        Authentication principal = authenticationFor(secUser());
        when(headerAccessor.getSessionId()).thenReturn("session-1");

        DashboardSubscribeRequest request = new DashboardSubscribeRequest();
        request.setPage(-5);

        controller.subscribeDashboard(request, principal, headerAccessor);

        ArgumentCaptor<DashboardSubscription> captor = ArgumentCaptor.forClass(DashboardSubscription.class);
        verify(registry).registerDashboard(captor.capture());
        assertThat(captor.getValue().page()).isEqualTo(0);
    }

    @Test
    void subscribeDashboard_nullPrincipal_doesNotRegister() {
        when(headerAccessor.getSessionId()).thenReturn("session-1");

        controller.subscribeDashboard(new DashboardSubscribeRequest(), null, headerAccessor);

        verifyNoInteractions(registry);
    }

    @Test
    void subscribeDashboard_nonAuthenticationPrincipal_doesNotRegister() {
        when(headerAccessor.getSessionId()).thenReturn("session-1");
        Principal notAnAuthentication = () -> "someone";

        controller.subscribeDashboard(new DashboardSubscribeRequest(), notAnAuthentication, headerAccessor);

        verifyNoInteractions(registry);
    }

    @Test
    void subscribeDashboard_authenticationWithoutSecUserPrincipal_doesNotRegister() {
        when(headerAccessor.getSessionId()).thenReturn("session-1");
        Authentication principal = new TestingAuthenticationToken("not-a-secuser", null);

        controller.subscribeDashboard(new DashboardSubscribeRequest(), principal, headerAccessor);

        verifyNoInteractions(registry);
    }

    @Test
    void subscribeDashboard_nullSessionId_doesNotRegister() {
        Authentication principal = authenticationFor(secUser());
        when(headerAccessor.getSessionId()).thenReturn(null);

        controller.subscribeDashboard(new DashboardSubscribeRequest(), principal, headerAccessor);

        verifyNoInteractions(registry);
    }

    @Test
    void subscribeForecast_validRequest_registersSubscription() {
        SecUser user = secUser();
        Authentication principal = authenticationFor(user);
        when(headerAccessor.getSessionId()).thenReturn("session-1");

        ForecastSubscribeRequest request = new ForecastSubscribeRequest();
        request.setLat(50.45);
        request.setLon(30.52);

        controller.subscribeForecast(request, principal, headerAccessor);

        ArgumentCaptor<ForecastSubscription> captor = ArgumentCaptor.forClass(ForecastSubscription.class);
        verify(registry).registerForecast(captor.capture());
        ForecastSubscription subscription = captor.getValue();
        assertThat(subscription.sessionId()).isEqualTo("session-1");
        assertThat(subscription.userId()).isEqualTo(user.getId());
        assertThat(subscription.username()).isEqualTo("john@example.com");
        assertThat(subscription.units()).isEqualTo(UnitSystem.METRIC);
        assertThat(subscription.latitude()).isEqualTo(50.45);
        assertThat(subscription.longitude()).isEqualTo(30.52);
    }

    @Test
    void subscribeForecast_missingLatitude_doesNotRegister() {
        Authentication principal = authenticationFor(secUser());
        when(headerAccessor.getSessionId()).thenReturn("session-1");

        ForecastSubscribeRequest request = new ForecastSubscribeRequest();
        request.setLon(30.52);

        controller.subscribeForecast(request, principal, headerAccessor);

        verifyNoInteractions(registry);
    }

    @Test
    void subscribeForecast_missingLongitude_doesNotRegister() {
        Authentication principal = authenticationFor(secUser());
        when(headerAccessor.getSessionId()).thenReturn("session-1");

        ForecastSubscribeRequest request = new ForecastSubscribeRequest();
        request.setLat(50.45);

        controller.subscribeForecast(request, principal, headerAccessor);

        verifyNoInteractions(registry);
    }

    @Test
    void subscribeForecast_nullPrincipal_doesNotRegister() {
        when(headerAccessor.getSessionId()).thenReturn("session-1");

        ForecastSubscribeRequest request = new ForecastSubscribeRequest();
        request.setLat(50.45);
        request.setLon(30.52);

        controller.subscribeForecast(request, null, headerAccessor);

        verifyNoInteractions(registry);
    }

    @Test
    void subscribeForecast_nullSessionId_doesNotRegister() {
        Authentication principal = authenticationFor(secUser());
        when(headerAccessor.getSessionId()).thenReturn(null);

        ForecastSubscribeRequest request = new ForecastSubscribeRequest();
        request.setLat(50.45);
        request.setLon(30.52);

        controller.subscribeForecast(request, principal, headerAccessor);

        verifyNoInteractions(registry);
    }

}
