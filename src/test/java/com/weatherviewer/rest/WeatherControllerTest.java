package com.weatherviewer.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import com.weatherviewer.service.WeatherApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
@WithMockUser
class WeatherControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    WeatherApiService weatherApiService;

    private WeatherDto weatherDto() {
        return new WeatherDto()
                .setWeatherCondition(WeatherCondition.CLEAR)
                .setTimeOfDay(TimeOfDay.DAY)
                .setDescription("Clear sky")
                .setTemperature(25.0)
                .setTemperatureFeelsLike(24.0)
                .setDate(new Date());
    }

    private LocationDto locationDto() {
        return new LocationDto()
                .setId(UUID.randomUUID())
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUserId(UUID.randomUUID())
                .setFavorite(false)
                .setCreatedAt(java.time.LocalDateTime.now());
    }

    @Test
    void getWeatherByLocation_returns200() throws Exception {
        LocationDto dto = locationDto();
        when(weatherApiService.getWeatherByLocation(any(LocationDto.class))).thenReturn(weatherDto());

        mockMvc.perform(get("/api/v1/weather/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(25.0));
    }

    @Test
    void getWeatherByCity_returns200() throws Exception {
        when(weatherApiService.getWeatherByCity("Kyiv")).thenReturn(weatherDto());

        mockMvc.perform(get("/api/v1/weather/city")
                        .param("city", "Kyiv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(25.0));
    }

    @Test
    void getWeatherByCity_blankCity_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/weather/city")
                        .param("city", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWeatherByCoordinates_returns200() throws Exception {
        when(weatherApiService.getWeatherByCoordinates(50.45, 30.52)).thenReturn(weatherDto());

        mockMvc.perform(get("/api/v1/weather/coord")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperature").value(25.0));
    }

    @Test
    void getWeatherByCoordinates_invalidLatitude_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/weather/coord")
                        .param("latitude", "91.0")
                        .param("longitude", "30.52"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWeatherByCoordinates_invalidLongitude_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/weather/coord")
                        .param("latitude", "50.45")
                        .param("longitude", "181.0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDailyForecastByCity_returns200() throws Exception {
        when(weatherApiService.getDailyForecastByCity("Kyiv")).thenReturn(List.of(weatherDto()));

        mockMvc.perform(get("/api/v1/weather/daily/city")
                        .param("city", "Kyiv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].temperature").value(25.0));
    }

    @Test
    void getDailyForecastByCity_blankCity_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/weather/daily/city")
                        .param("city", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDailyForecastByCoordinates_returns200() throws Exception {
        when(weatherApiService.getDailyForecastByCoordinates(50.45, 30.52)).thenReturn(List.of(weatherDto()));

        mockMvc.perform(get("/api/v1/weather/daily/coord")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].temperature").value(25.0));
    }

    @Test
    void getHourlyForecastByCity_returns200() throws Exception {
        when(weatherApiService.getHourlyForecastByCity("Kyiv")).thenReturn(List.of(weatherDto()));

        mockMvc.perform(get("/api/v1/weather/hourly/city")
                        .param("city", "Kyiv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].temperature").value(25.0));
    }

    @Test
    void getHourlyForecastByCity_blankCity_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/weather/hourly/city")
                        .param("city", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getHourlyForecastByCoordinates_returns200() throws Exception {
        when(weatherApiService.getHourlyForecastByCoordinates(50.45, 30.52)).thenReturn(List.of(weatherDto()));

        mockMvc.perform(get("/api/v1/weather/hourly/coord")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].temperature").value(25.0));
    }

    @Test
    void getCitiesByName_returns200() throws Exception {
        List<GeoLocationDto> dtos = List.of(new GeoLocationDto().setName("Kyiv"));
        when(weatherApiService.getCitiesByName("Kyiv")).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/weather/city-search")
                        .param("city", "Kyiv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Kyiv"));
    }

    @Test
    void getCitiesByName_blankCity_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/weather/city-search")
                        .param("city", ""))
                .andExpect(status().isBadRequest());
    }

}
