package com.weatherviewer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.service.helper.UnitConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForecastController.class)
class ForecastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ForecastController forecastController;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WeatherApiService weatherApiService;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private UnitConverter unitConverter;

    @MockitoBean
    private MessageSource messageSource;

    @BeforeEach
    void setup() {
        when(unitConverter.toDisplayUnits(anyList(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(unitConverter.temperatureSymbol(any())).thenReturn("°C");
        when(unitConverter.windSpeedUnit(any())).thenReturn("m/s");

        when(messageSource.getMessage(any(String.class), any(), any(Locale.class)))
                .thenAnswer(inv -> "Label for " + inv.getArgument(0));
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
    void getForecast_returns200AndViewAndPopulatesAllModelAttributes() throws Exception {
        SecUser user = secUser();
        LocationDto location = locationDto();
        WeatherDto weather = weatherDto();

        when(locationService.getByCoordinatesAndUserId(50.45, 30.52, user.getId()))
                .thenReturn(location);
        when(weatherApiService.getHourlyForecastByCoordinates(50.45, 30.52))
                .thenReturn(List.of(weather));
        when(weatherApiService.getDailyForecastByCoordinates(50.45, 30.52))
                .thenReturn(List.of(weather));

        mockMvc.perform(get("/forecast")
                        .with(user(user))
                        .param("lat", "50.45")
                        .param("lon", "30.52"))
                .andExpect(status().isOk())
                .andExpect(view().name("forecast"))
                .andExpect(model().attribute("latitude", 50.45))
                .andExpect(model().attribute("longitude", 30.52))
                .andExpect(model().attribute("locationName", "Kyiv"))
                .andExpect(model().attribute("hourlyForecast", List.of(weather)))
                .andExpect(model().attribute("dailyForecast", List.of(weather)))
                .andExpect(model().attribute("login", "John Doe"))
                .andExpect(model().attribute("temperatureSymbol", "°C"))
                .andExpect(model().attribute("windSpeedUnit", "m/s"))
                .andExpect(model().attributeExists("conditionLabelsJson"));
    }

    @Test
    void getForecast_whenObjectMapperFails_returnsEmptyJsonInModel() throws Exception {
        SecUser user = secUser();
        LocationDto location = locationDto();

        when(locationService.getByCoordinatesAndUserId(50.45, 30.52, user.getId()))
                .thenReturn(location);

        ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
        when(failingObjectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Serialization failed") {});

        ReflectionTestUtils.setField(forecastController, "objectMapper", failingObjectMapper);

        try {
            mockMvc.perform(get("/forecast")
                            .with(user(user))
                            .param("lat", "50.45")
                            .param("lon", "30.52"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("forecast"))
                    .andExpect(model().attribute("conditionLabelsJson", "{}"));
        } finally {
            // Restore the real ObjectMapper for any other tests
            ReflectionTestUtils.setField(forecastController, "objectMapper", objectMapper);
        }
    }

    @Test
    void getForecast_invalidLatitude_returns3xxRedirection() throws Exception {
        SecUser user = secUser();

        mockMvc.perform(get("/forecast")
                        .with(user(user))
                        .param("lat", "91.0")
                        .param("lon", "30.52"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void getForecast_invalidLongitude_returns3xxRedirection() throws Exception {
        SecUser user = secUser();

        mockMvc.perform(get("/forecast")
                        .with(user(user))
                        .param("lat", "50.45")
                        .param("lon", "181.0"))
                .andExpect(status().is3xxRedirection());
    }

}
