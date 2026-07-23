package com.weatherviewer.controller;

import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.dto.WeatherDto;
import com.weatherviewer.dto.enums.TimeOfDay;
import com.weatherviewer.dto.enums.WeatherCondition;
import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

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
                .setCreatedAt(java.time.LocalDateTime.now());
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
    void home_returns200AndHomeView() throws Exception {
        SecUser secUser = secUser();
        when(locationService.getByUserIdSortedByDate(secUser.getId())).thenReturn(List.of());

        mockMvc.perform(get("/").with(user(secUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("login", "sort"));
    }

    @Test
    void home_withLocations_addsWeatherMap() throws Exception {
        SecUser secUser = secUser();
        LocationDto location = locationDto();
        WeatherDto weather = weatherDto();

        when(locationService.getByUserIdSortedByDate(secUser.getId())).thenReturn(List.of(location));
        when(weatherApiService.getWeatherByLocation(location)).thenReturn(weather);

        mockMvc.perform(get("/").with(user(secUser)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("locationWeatherMap"));
    }

    @Test
    void home_sortNameAsc_callsCorrectService() throws Exception {
        SecUser secUser = secUser();
        when(locationService.getByUserIdSortedByNameAsc(secUser.getId())).thenReturn(List.of());

        mockMvc.perform(get("/").with(user(secUser)).param("sort", "nameAsc"))
                .andExpect(status().isOk());

        verify(locationService).getByUserIdSortedByNameAsc(secUser.getId());
    }

    @Test
    void home_sortNameDesc_callsCorrectService() throws Exception {
        SecUser secUser = secUser();
        when(locationService.getByUserIdSortedByNameDesc(secUser.getId())).thenReturn(List.of());

        mockMvc.perform(get("/").with(user(secUser)).param("sort", "nameDesc"))
                .andExpect(status().isOk());

        verify(locationService).getByUserIdSortedByNameDesc(secUser.getId());
    }

    @Test
    void home_sortFavoriteFirst_callsCorrectService() throws Exception {
        SecUser secUser = secUser();
        when(locationService.getByUserIdSortedByFavorite(secUser.getId())).thenReturn(List.of());

        mockMvc.perform(get("/").with(user(secUser)).param("sort", "favoriteFirst"))
                .andExpect(status().isOk());

        verify(locationService).getByUserIdSortedByFavorite(secUser.getId());
    }

    @Test
    void home_sortFavoritesOnly_callsCorrectService() throws Exception {
        SecUser secUser = secUser();
        when(locationService.getFavoritesByUserId(secUser.getId())).thenReturn(List.of());

        mockMvc.perform(get("/").with(user(secUser)).param("sort", "favoritesOnly"))
                .andExpect(status().isOk());

        verify(locationService).getFavoritesByUserId(secUser.getId());
    }

    @Test
    void home_duplicateLocations_mergesFavoringExisting() throws Exception {
        SecUser secUser = secUser();
        LocationDto location = locationDto();
        WeatherDto weather = weatherDto();

        when(locationService.getByUserIdSortedByDate(secUser.getId()))
                .thenReturn(List.of(location, location));
        when(weatherApiService.getWeatherByLocation(location)).thenReturn(weather);

        mockMvc.perform(get("/").with(user(secUser)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("locationWeatherMap"));
    }

    @Test
    void home_unauthenticated_redirectsToSignIn() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteLocation_redirectsToHome() throws Exception {
        SecUser secUser = secUser();
        UUID locationId = UUID.randomUUID();

        mockMvc.perform(delete("/locations/{id}", locationId)
                        .with(user(secUser))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/?sort=*"));

        verify(locationService).deleteByIdAndUserId(locationId, secUser.getId());
    }

    @Test
    void addToFavorite_redirectsToHome() throws Exception {
        SecUser secUser = secUser();
        UUID locationId = UUID.randomUUID();

        mockMvc.perform(post("/locations/{id}/favorite", locationId)
                        .with(user(secUser))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/?sort=*"));

        verify(locationService).addToFavorite(locationId, secUser.getId());
    }

    @Test
    void removeFromFavorite_redirectsToHome() throws Exception {
        SecUser secUser = secUser();
        UUID locationId = UUID.randomUUID();

        mockMvc.perform(delete("/locations/{id}/favorite", locationId)
                        .with(user(secUser))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/?sort=*"));

        verify(locationService).removeFromFavorite(locationId, secUser.getId());
    }

}
