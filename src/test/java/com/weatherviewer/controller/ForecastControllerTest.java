package com.weatherviewer.controller;

import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForecastController.class)
class ForecastControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WeatherApiService weatherApiService;

    @MockitoBean
    LocationService locationService;

    private SecUser secUser() {
        return new SecUser(
                UUID.randomUUID(),
                "john@example.com",
                "hashed",
                Set.of(),
                true,
                "John Doe"
        );
    }

    private LocationDto locationDto() {
        return new LocationDto()
                .setId(UUID.randomUUID())
                .setName("Kyiv")
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
    void getForecast_returns200AndView() throws Exception {
        SecUser user = secUser();
        LocationDto location = locationDto();

        when(locationService.getByCoordinatesAndUserId(50.45, 30.52, user.getId()))
                .thenReturn(location);
        when(weatherApiService.getHourlyForecastByCoordinates(50.45, 30.52))
                .thenReturn(List.of(weatherDto()));
        when(weatherApiService.getDailyForecastByCoordinates(50.45, 30.52))
                .thenReturn(List.of(weatherDto()));

        mockMvc.perform(get("/forecast")
                        .with(user(user))
                        .param("lat", "50.45")
                        .param("lon", "30.52"))
                .andExpect(status().isOk())
                .andExpect(view().name("forecast"))
                .andExpect(model().attributeExists("locationName", "hourlyForecast", "dailyForecast", "login"));
    }

    @Test
    void getForecast_addsCorrectLocationName() throws Exception {
        SecUser user = secUser();
        LocationDto location = locationDto();

        when(locationService.getByCoordinatesAndUserId(50.45, 30.52, user.getId()))
                .thenReturn(location);
        when(weatherApiService.getHourlyForecastByCoordinates(50.45, 30.52))
                .thenReturn(List.of());
        when(weatherApiService.getDailyForecastByCoordinates(50.45, 30.52))
                .thenReturn(List.of());

        mockMvc.perform(get("/forecast")
                        .with(user(user))
                        .param("lat", "50.45")
                        .param("lon", "30.52"))
                .andExpect(model().attribute("locationName", "Kyiv"))
                .andExpect(model().attribute("login", "John Doe"));
    }

    @Test
    void getForecast_invalidLatitude_returns400() throws Exception {
        SecUser user = secUser();

        mockMvc.perform(get("/forecast")
                        .with(user(user))
                        .param("lat", "91.0")
                        .param("lon", "30.52"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void getForecast_invalidLongitude_returns400() throws Exception {
        SecUser user = secUser();

        mockMvc.perform(get("/forecast")
                        .with(user(user))
                        .param("lat", "50.45")
                        .param("lon", "181.0"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void getForecast_addsHourlyAndDailyForecast() throws Exception {
        SecUser user = secUser();
        WeatherDto weather = weatherDto();

        when(locationService.getByCoordinatesAndUserId(50.45, 30.52, user.getId()))
                .thenReturn(locationDto());
        when(weatherApiService.getHourlyForecastByCoordinates(50.45, 30.52))
                .thenReturn(List.of(weather));
        when(weatherApiService.getDailyForecastByCoordinates(50.45, 30.52))
                .thenReturn(List.of(weather));

        mockMvc.perform(get("/forecast")
                        .with(user(user))
                        .param("lat", "50.45")
                        .param("lon", "30.52"))
                .andExpect(model().attribute("hourlyForecast", List.of(weather)))
                .andExpect(model().attribute("dailyForecast", List.of(weather)));
    }

}
