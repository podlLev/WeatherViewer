package com.weatherviewer.websocket;

import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.ws.DashboardLocationWeather;
import com.weatherviewer.dto.ws.DashboardUpdateMessage;
import com.weatherviewer.dto.ws.ForecastUpdateMessage;
import com.weatherviewer.exception.notfound.LocationNotFoundException;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.service.helper.UnitConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Drives every connected client's live weather feed.
 * <p>
 * On a fixed schedule, this re-derives exactly what each active
 * {@link DashboardSubscription}/{@link ForecastSubscription} should
 * currently be showing (same lookups {@code HomeController} and
 * {@code ForecastController} do for a full page load) and pushes it to
 * that user's private STOMP queue. Weather fetches for the individual
 * subscriptions are composed as a single {@link Flux} with bounded
 * concurrency, so one broadcast tick can't fan out an unbounded burst of
 * calls into {@link WeatherApiService} (which mostly resolves from the
 * Redis-backed cache anyway, but a cache stampede past the TTL boundary is
 * still worth capping). Each subscription's fetch/push is isolated with
 * {@code onErrorResume}, so one user's failing/slow location can't stall or
 * cancel the whole tick.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherLiveUpdateScheduler {

    private final WeatherSubscriptionRegistry registry;
    private final LocationService locationService;
    private final WeatherApiService weatherApiService;
    private final UnitConverter unitConverter;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExecutorService weatherFetchExecutor;

    @Value("${location.dashboard.page-size:12}")
    private int dashboardPageSize;

    @Value("${weather.live.max-concurrent-fetches:16}")
    private int maxConcurrentFetches;

    /**
     * Runs one broadcast tick. {@code fixedDelayString} (not fixed-rate) so
     * that if a tick ever runs long - a slow OpenWeatherMap response, a
     * burst of subscribers - the next tick is scheduled relative to when
     * this one actually finished, instead of piling up overlapping ticks.
     */
    @Scheduled(fixedDelayString = "${weather.live.push-interval-ms:60000}")
    public void broadcast() {
        Scheduler fetchScheduler = Schedulers.fromExecutor(weatherFetchExecutor);

        Flux<Void> dashboardTicks = Flux.fromIterable(registry.dashboardSubscriptions())
                .flatMap(subscription -> pushDashboardUpdate(subscription, fetchScheduler), maxConcurrentFetches);

        Flux<Void> forecastTicks = Flux.fromIterable(registry.forecastSubscriptions())
                .flatMap(subscription -> pushForecastUpdate(subscription, fetchScheduler), maxConcurrentFetches);

        Flux.merge(dashboardTicks, forecastTicks).then().block();
    }

    private Mono<Void> pushDashboardUpdate(DashboardSubscription subscription, Scheduler fetchScheduler) {
        return Mono.fromRunnable(() -> doPushDashboardUpdate(subscription))
                .subscribeOn(fetchScheduler)
                .then()
                .onErrorResume(ex -> {
                    log.warn("Live dashboard push failed for user={}: {}", subscription.username(), ex.getMessage());
                    return Mono.empty();
                });
    }

    private void doPushDashboardUpdate(DashboardSubscription subscription) {
        Page<LocationDto> locationPage = locationService.getByUserIdSorted(
                subscription.userId(), subscription.sort(), PageRequest.of(subscription.page(), dashboardPageSize));

        List<DashboardLocationWeather> updates = new ArrayList<>();
        List<String> unavailable = new ArrayList<>();

        for (LocationDto location : locationPage.getContent()) {
            try {
                WeatherDto weather = unitConverter.toDisplayUnits(
                        weatherApiService.getWeatherByLocation(location), subscription.units());
                updates.add(new DashboardLocationWeather()
                        .setLocationId(location.getId())
                        .setLocationName(location.getName())
                        .setWeather(weather));
            } catch (RuntimeException ex) {
                log.debug("Live weather fetch failed for location '{}' (user={}): {}",
                        location.getName(), subscription.username(), ex.getMessage());
                unavailable.add(location.getName());
            }
        }

        if (updates.isEmpty() && unavailable.isEmpty()) {
            return;
        }

        messagingTemplate.convertAndSendToUser(subscription.username(), "/queue/dashboard",
                new DashboardUpdateMessage().setLocations(updates).setUnavailableLocationNames(unavailable));
    }

    private Mono<Void> pushForecastUpdate(ForecastSubscription subscription, Scheduler fetchScheduler) {
        return Mono.fromRunnable(() -> doPushForecastUpdate(subscription))
                .subscribeOn(fetchScheduler)
                .then()
                .onErrorResume(ex -> {
                    log.warn("Live forecast push failed for user={}: {}", subscription.username(), ex.getMessage());
                    return Mono.empty();
                });
    }

    private void doPushForecastUpdate(ForecastSubscription subscription) {
        try {
            locationService.getByCoordinatesAndUserId(subscription.latitude(), subscription.longitude(), subscription.userId());
        } catch (LocationNotFoundException ex) {
            log.debug("Live forecast subscription for user={} no longer owns lat={}, lon={}; skipping tick",
                    subscription.username(), subscription.latitude(), subscription.longitude());
            return;
        }

        List<WeatherDto> hourly = unitConverter.toDisplayUnits(
                weatherApiService.getHourlyForecastByCoordinates(subscription.latitude(), subscription.longitude()), subscription.units());
        List<WeatherDto> daily = unitConverter.toDisplayUnits(
                weatherApiService.getDailyForecastByCoordinates(subscription.latitude(), subscription.longitude()), subscription.units());

        messagingTemplate.convertAndSendToUser(subscription.username(), "/queue/forecast",
                new ForecastUpdateMessage().setHourlyForecast(hourly).setDailyForecast(daily));
    }

}
