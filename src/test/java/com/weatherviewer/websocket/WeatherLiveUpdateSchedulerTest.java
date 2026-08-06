package com.weatherviewer.websocket;

import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import com.weatherviewer.dto.ws.DashboardUpdateMessage;
import com.weatherviewer.dto.ws.ForecastUpdateMessage;
import com.weatherviewer.exception.notfound.LocationNotFoundException;
import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.service.helper.UnitConverter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherLiveUpdateSchedulerTest {

    private static final int DASHBOARD_PAGE_SIZE = 12;
    private static final int MAX_CONCURRENT_FETCHES = 16;

    @Mock
    private WeatherSubscriptionRegistry registry;

    @Mock
    private LocationService locationService;

    @Mock
    private WeatherApiService weatherApiService;

    @Mock
    private UnitConverter unitConverter;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ExecutorService weatherFetchExecutor;
    private WeatherLiveUpdateScheduler scheduler;

    @BeforeEach
    void setUp() {
        weatherFetchExecutor = Executors.newFixedThreadPool(2);
        scheduler = new WeatherLiveUpdateScheduler(registry, locationService, weatherApiService, unitConverter, messagingTemplate, weatherFetchExecutor);
        ReflectionTestUtils.setField(scheduler, "dashboardPageSize", DASHBOARD_PAGE_SIZE);
        ReflectionTestUtils.setField(scheduler, "maxConcurrentFetches", MAX_CONCURRENT_FETCHES);
    }

    @AfterEach
    void tearDown() {
        weatherFetchExecutor.shutdownNow();
    }

    private LocationDto locationDto(UUID id, String name) {
        return new LocationDto()
                .setId(id)
                .setName(name)
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUserId(UUID.randomUUID())
                .setFavorite(false)
                .setCreatedAt(LocalDateTime.now());
    }

    private WeatherDto weatherDto() {
        return new WeatherDto()
                .setWeatherCondition(WeatherCondition.CLEAR)
                .setTimeOfDay(TimeOfDay.DAY)
                .setDescription("Clear sky")
                .setTemperature(25.0)
                .setTemperatureFeelsLike(24.0)
                .setDate(new Date());
    }

    @Test
    void broadcast_noSubscriptions_sendsNothing() {
        when(registry.dashboardSubscriptions()).thenReturn(List.of());
        when(registry.forecastSubscriptions()).thenReturn(List.of());

        scheduler.broadcast();

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void broadcast_dashboardSubscription_pushesConvertedWeatherForEachLocation() {
        UUID userId = UUID.randomUUID();
        UUID locationId = UUID.randomUUID();
        DashboardSubscription subscription = new DashboardSubscription(
                "session-1", userId, "john@example.com", UnitSystem.IMPERIAL, "nameAsc", 1);
        LocationDto location = locationDto(locationId, "Kyiv");
        WeatherDto rawWeather = weatherDto();
        WeatherDto convertedWeather = weatherDto().setTemperature(77.0);

        when(registry.dashboardSubscriptions()).thenReturn(List.of(subscription));
        when(registry.forecastSubscriptions()).thenReturn(List.of());
        when(locationService.getByUserIdSorted(userId, "nameAsc", PageRequest.of(1, DASHBOARD_PAGE_SIZE)))
                .thenReturn(new PageImpl<>(List.of(location)));
        when(weatherApiService.getWeatherByLocation(location)).thenReturn(rawWeather);
        when(unitConverter.toDisplayUnits(rawWeather, UnitSystem.IMPERIAL)).thenReturn(convertedWeather);

        scheduler.broadcast();

        ArgumentCaptor<DashboardUpdateMessage> captor = ArgumentCaptor.forClass(DashboardUpdateMessage.class);
        verify(messagingTemplate).convertAndSendToUser(eq("john@example.com"), eq("/queue/dashboard"), captor.capture());
        DashboardUpdateMessage message = captor.getValue();
        assertThat(message.getLocations()).hasSize(1);
        assertThat(message.getLocations().get(0).getLocationId()).isEqualTo(locationId);
        assertThat(message.getLocations().get(0).getLocationName()).isEqualTo("Kyiv");
        assertThat(message.getLocations().get(0).getWeather()).isEqualTo(convertedWeather);
        assertThat(message.getUnavailableLocationNames()).isEmpty();
    }

    @Test
    void broadcast_dashboardLocationWeatherFetchFails_addsToUnavailableButStillPushes() {
        UUID userId = UUID.randomUUID();
        DashboardSubscription subscription = new DashboardSubscription(
                "session-1", userId, "john@example.com", UnitSystem.METRIC, "date", 0);
        LocationDto location = locationDto(UUID.randomUUID(), "Kyiv");

        when(registry.dashboardSubscriptions()).thenReturn(List.of(subscription));
        when(registry.forecastSubscriptions()).thenReturn(List.of());
        when(locationService.getByUserIdSorted(userId, "date", PageRequest.of(0, DASHBOARD_PAGE_SIZE)))
                .thenReturn(new PageImpl<>(List.of(location)));
        when(weatherApiService.getWeatherByLocation(location)).thenThrow(new RuntimeException("provider down"));

        scheduler.broadcast();

        ArgumentCaptor<DashboardUpdateMessage> captor = ArgumentCaptor.forClass(DashboardUpdateMessage.class);
        verify(messagingTemplate).convertAndSendToUser(eq("john@example.com"), eq("/queue/dashboard"), captor.capture());
        DashboardUpdateMessage message = captor.getValue();
        assertThat(message.getLocations()).isEmpty();
        assertThat(message.getUnavailableLocationNames()).containsExactly("Kyiv");
    }

    @Test
    void broadcast_dashboardSubscriptionWithNoLocationsOnPage_sendsNothing() {
        UUID userId = UUID.randomUUID();
        DashboardSubscription subscription = new DashboardSubscription(
                "session-1", userId, "john@example.com", UnitSystem.METRIC, "date", 0);

        when(registry.dashboardSubscriptions()).thenReturn(List.of(subscription));
        when(registry.forecastSubscriptions()).thenReturn(List.of());
        when(locationService.getByUserIdSorted(userId, "date", PageRequest.of(0, DASHBOARD_PAGE_SIZE)))
                .thenReturn(new PageImpl<>(List.of()));

        scheduler.broadcast();

        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void broadcast_oneDashboardSubscriptionFailsEntirely_doesNotPreventOthersFromPushing() {
        UUID failingUserId = UUID.randomUUID();
        UUID healthyUserId = UUID.randomUUID();
        DashboardSubscription failingSubscription = new DashboardSubscription(
                "session-1", failingUserId, "fails@example.com", UnitSystem.METRIC, "date", 0);
        DashboardSubscription healthySubscription = new DashboardSubscription(
                "session-2", healthyUserId, "healthy@example.com", UnitSystem.METRIC, "date", 0);
        LocationDto location = locationDto(UUID.randomUUID(), "Lviv");
        WeatherDto weather = weatherDto();

        when(registry.dashboardSubscriptions()).thenReturn(List.of(failingSubscription, healthySubscription));
        when(registry.forecastSubscriptions()).thenReturn(List.of());
        when(locationService.getByUserIdSorted(failingUserId, "date", PageRequest.of(0, DASHBOARD_PAGE_SIZE)))
                .thenThrow(new RuntimeException("db unavailable"));
        when(locationService.getByUserIdSorted(healthyUserId, "date", PageRequest.of(0, DASHBOARD_PAGE_SIZE)))
                .thenReturn(new PageImpl<>(List.of(location)));
        when(weatherApiService.getWeatherByLocation(location)).thenReturn(weather);
        when(unitConverter.toDisplayUnits(weather, UnitSystem.METRIC)).thenReturn(weather);

        scheduler.broadcast();

        verify(messagingTemplate).convertAndSendToUser(eq("healthy@example.com"), eq("/queue/dashboard"), any(DashboardUpdateMessage.class));
        verify(messagingTemplate, never()).convertAndSendToUser(eq("fails@example.com"), anyString(), any());
    }

    @Test
    void broadcast_forecastSubscription_pushesHourlyAndDailyForecast() {
        UUID userId = UUID.randomUUID();
        ForecastSubscription subscription = new ForecastSubscription(
                "session-1", userId, "john@example.com", UnitSystem.METRIC, 50.45, 30.52);
        List<WeatherDto> hourly = List.of(weatherDto());
        List<WeatherDto> daily = List.of(weatherDto());

        when(registry.dashboardSubscriptions()).thenReturn(List.of());
        when(registry.forecastSubscriptions()).thenReturn(List.of(subscription));
        when(locationService.getByCoordinatesAndUserId(50.45, 30.52, userId)).thenReturn(locationDto(UUID.randomUUID(), "Kyiv"));
        when(weatherApiService.getHourlyForecastByCoordinates(50.45, 30.52)).thenReturn(hourly);
        when(weatherApiService.getDailyForecastByCoordinates(50.45, 30.52)).thenReturn(daily);
        when(unitConverter.toDisplayUnits(hourly, UnitSystem.METRIC)).thenReturn(hourly);
        when(unitConverter.toDisplayUnits(daily, UnitSystem.METRIC)).thenReturn(daily);

        scheduler.broadcast();

        ArgumentCaptor<ForecastUpdateMessage> captor = ArgumentCaptor.forClass(ForecastUpdateMessage.class);
        verify(messagingTemplate).convertAndSendToUser(eq("john@example.com"), eq("/queue/forecast"), captor.capture());
        assertThat(captor.getValue().getHourlyForecast()).isEqualTo(hourly);
        assertThat(captor.getValue().getDailyForecast()).isEqualTo(daily);
    }

    @Test
    void broadcast_forecastLocationNoLongerOwnedByUser_skipsTickWithoutPushing() {
        UUID userId = UUID.randomUUID();
        ForecastSubscription subscription = new ForecastSubscription(
                "session-1", userId, "john@example.com", UnitSystem.METRIC, 50.45, 30.52);

        when(registry.dashboardSubscriptions()).thenReturn(List.of());
        when(registry.forecastSubscriptions()).thenReturn(List.of(subscription));
        when(locationService.getByCoordinatesAndUserId(50.45, 30.52, userId))
                .thenThrow(new LocationNotFoundException("not found"));

        scheduler.broadcast();

        verifyNoInteractions(messagingTemplate);
        verifyNoInteractions(weatherApiService);
    }

    @Test
    void broadcast_forecastSubscriptionThrowsUnexpectedException_isIsolatedAndDoesNotPreventOtherForecastPush() {
        UUID failingUserId = UUID.randomUUID();
        UUID healthyUserId = UUID.randomUUID();
        ForecastSubscription failingSubscription = new ForecastSubscription(
                "session-1", failingUserId, "fails@example.com", UnitSystem.METRIC, 50.45, 30.52);
        ForecastSubscription healthySubscription = new ForecastSubscription(
                "session-2", healthyUserId, "healthy@example.com", UnitSystem.METRIC, 51.51, -0.13);
        List<WeatherDto> forecastList = List.of(weatherDto());

        when(registry.dashboardSubscriptions()).thenReturn(List.of());
        when(registry.forecastSubscriptions()).thenReturn(List.of(failingSubscription, healthySubscription));

        when(locationService.getByCoordinatesAndUserId(50.45, 30.52, failingUserId))
                .thenReturn(locationDto(UUID.randomUUID(), "Kyiv"));
        when(weatherApiService.getHourlyForecastByCoordinates(50.45, 30.52))
                .thenThrow(new RuntimeException("provider down"));

        when(locationService.getByCoordinatesAndUserId(51.51, -0.13, healthyUserId))
                .thenReturn(locationDto(UUID.randomUUID(), "London"));
        when(weatherApiService.getHourlyForecastByCoordinates(51.51, -0.13)).thenReturn(forecastList);
        when(weatherApiService.getDailyForecastByCoordinates(51.51, -0.13)).thenReturn(forecastList);
        when(unitConverter.toDisplayUnits(forecastList, UnitSystem.METRIC)).thenReturn(forecastList);

        scheduler.broadcast();

        verify(messagingTemplate, never()).convertAndSendToUser(eq("fails@example.com"), anyString(), any());
        verify(messagingTemplate).convertAndSendToUser(eq("healthy@example.com"), eq("/queue/forecast"), any(ForecastUpdateMessage.class));
    }

    @Test
    void broadcast_dashboardAndForecastSubscriptionsTogether_pushesBoth() {
        UUID dashboardUserId = UUID.randomUUID();
        UUID forecastUserId = UUID.randomUUID();
        DashboardSubscription dashboardSubscription = new DashboardSubscription(
                "session-1", dashboardUserId, "dashboard@example.com", UnitSystem.METRIC, "date", 0);
        ForecastSubscription forecastSubscription = new ForecastSubscription(
                "session-2", forecastUserId, "forecast@example.com", UnitSystem.METRIC, 50.45, 30.52);
        LocationDto dashboardLocation = locationDto(UUID.randomUUID(), "Kyiv");
        WeatherDto weather = weatherDto();
        List<WeatherDto> forecastList = List.of(weatherDto());

        when(registry.dashboardSubscriptions()).thenReturn(List.of(dashboardSubscription));
        when(registry.forecastSubscriptions()).thenReturn(List.of(forecastSubscription));
        when(locationService.getByUserIdSorted(dashboardUserId, "date", PageRequest.of(0, DASHBOARD_PAGE_SIZE)))
                .thenReturn(new PageImpl<>(List.of(dashboardLocation)));
        when(weatherApiService.getWeatherByLocation(dashboardLocation)).thenReturn(weather);
        when(unitConverter.toDisplayUnits(weather, UnitSystem.METRIC)).thenReturn(weather);
        when(locationService.getByCoordinatesAndUserId(50.45, 30.52, forecastUserId))
                .thenReturn(locationDto(UUID.randomUUID(), "Lviv"));
        when(weatherApiService.getHourlyForecastByCoordinates(50.45, 30.52)).thenReturn(forecastList);
        when(weatherApiService.getDailyForecastByCoordinates(50.45, 30.52)).thenReturn(forecastList);
        when(unitConverter.toDisplayUnits(forecastList, UnitSystem.METRIC)).thenReturn(forecastList);

        scheduler.broadcast();

        verify(messagingTemplate).convertAndSendToUser(eq("dashboard@example.com"), eq("/queue/dashboard"), any(DashboardUpdateMessage.class));
        verify(messagingTemplate).convertAndSendToUser(eq("forecast@example.com"), eq("/queue/forecast"), any(ForecastUpdateMessage.class));
    }

}
