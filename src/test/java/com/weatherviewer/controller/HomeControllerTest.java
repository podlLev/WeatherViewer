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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
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

    private Page<LocationDto> pageOf(List<LocationDto> locations, Pageable pageable) {
        return new PageImpl<>(locations, pageable, locations.size());
    }

    @Test
    void home_returns200AndHomeView() throws Exception {
        SecUser secUser = secUser();
        when(locationService.getByUserIdSorted(eq(secUser.getId()), anyString(), any(Pageable.class)))
                .thenReturn(pageOf(List.of(), PageRequest.of(0, 12)));

        mockMvc.perform(get("/").with(user(secUser)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("login", "sort", "currentPage", "totalPages"));
    }

    @Test
    void home_withLocations_addsWeatherMap() throws Exception {
        SecUser secUser = secUser();
        LocationDto location = locationDto();
        WeatherDto weather = weatherDto();

        when(locationService.getByUserIdSorted(eq(secUser.getId()), anyString(), any(Pageable.class)))
                .thenReturn(pageOf(List.of(location), PageRequest.of(0, 12)));
        when(weatherApiService.getWeatherByLocation(location)).thenReturn(weather);

        mockMvc.perform(get("/").with(user(secUser)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("locationWeatherMap"));
    }

    @Test
    void home_sortNameAsc_passesSortToService() throws Exception {
        SecUser secUser = secUser();
        when(locationService.getByUserIdSorted(eq(secUser.getId()), eq("nameAsc"), any(Pageable.class)))
                .thenReturn(pageOf(List.of(), PageRequest.of(0, 12)));

        mockMvc.perform(get("/").with(user(secUser)).param("sort", "nameAsc"))
                .andExpect(status().isOk());

        verify(locationService).getByUserIdSorted(eq(secUser.getId()), eq("nameAsc"), any(Pageable.class));
    }

    @Test
    void home_pageParam_isForwardedToService() throws Exception {
        SecUser secUser = secUser();
        when(locationService.getByUserIdSorted(eq(secUser.getId()), anyString(), eq(PageRequest.of(2, 12))))
                .thenReturn(pageOf(List.of(), PageRequest.of(2, 12)));

        mockMvc.perform(get("/").with(user(secUser)).param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 2));

        verify(locationService).getByUserIdSorted(eq(secUser.getId()), anyString(), eq(PageRequest.of(2, 12)));
    }

    @Test
    void home_negativePageParam_isClampedToZero() throws Exception {
        SecUser secUser = secUser();
        when(locationService.getByUserIdSorted(eq(secUser.getId()), anyString(), eq(PageRequest.of(0, 12))))
                .thenReturn(pageOf(List.of(), PageRequest.of(0, 12)));

        mockMvc.perform(get("/").with(user(secUser)).param("page", "-5"))
                .andExpect(status().isOk());

        verify(locationService).getByUserIdSorted(eq(secUser.getId()), anyString(), eq(PageRequest.of(0, 12)));
    }

    @Test
    void home_paginationAttributes_reflectPageResult() throws Exception {
        SecUser secUser = secUser();
        LocationDto location = locationDto();
        WeatherDto weather = weatherDto();
        Page<LocationDto> page = new PageImpl<>(List.of(location), PageRequest.of(0, 12), 25);

        when(locationService.getByUserIdSorted(eq(secUser.getId()), anyString(), any(Pageable.class)))
                .thenReturn(page);
        when(weatherApiService.getWeatherByLocation(location)).thenReturn(weather);

        mockMvc.perform(get("/").with(user(secUser)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalPages", page.getTotalPages()))
                .andExpect(model().attribute("totalLocations", page.getTotalElements()))
                .andExpect(model().attribute("hasNextPage", true))
                .andExpect(model().attribute("hasPreviousPage", false));
    }

    @Test
    void home_oneLocationFailsToFetch_othersStillRenderAndFailureIsFlagged() throws Exception {
        SecUser secUser = secUser();
        LocationDto okLocation = locationDto().setName("Kyiv");
        LocationDto failingLocation = locationDto().setName("Atlantis");
        WeatherDto weather = weatherDto();

        when(locationService.getByUserIdSorted(eq(secUser.getId()), anyString(), any(Pageable.class)))
                .thenReturn(pageOf(List.of(okLocation, failingLocation), PageRequest.of(0, 12)));
        when(weatherApiService.getWeatherByLocation(okLocation)).thenReturn(weather);
        when(weatherApiService.getWeatherByLocation(failingLocation))
                .thenThrow(new RuntimeException("provider unavailable"));

        mockMvc.perform(get("/").with(user(secUser)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("locationWeatherMap"))
                .andExpect(model().attributeExists("errorMessages"));
    }

    @Test
    void home_unauthenticated_redirectsToSignIn() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void deleteLocation_redirectsToHomePreservingSortAndPage() throws Exception {
        SecUser secUser = secUser();
        UUID locationId = UUID.randomUUID();

        mockMvc.perform(delete("/locations/{id}", locationId)
                        .with(user(secUser))
                        .with(csrf())
                        .param("sort", "nameAsc")
                        .param("page", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?sort=nameAsc&page=1"));

        verify(locationService).deleteByIdAndUserId(locationId, secUser.getId());
    }

    @Test
    void addToFavorite_redirectsToHomePreservingSortAndPage() throws Exception {
        SecUser secUser = secUser();
        UUID locationId = UUID.randomUUID();

        mockMvc.perform(post("/locations/{id}/favorite", locationId)
                        .with(user(secUser))
                        .with(csrf())
                        .param("sort", "favoriteFirst")
                        .param("page", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?sort=favoriteFirst&page=3"));

        verify(locationService).addToFavorite(locationId, secUser.getId());
    }

    @Test
    void removeFromFavorite_redirectsToHomePreservingSortAndPage() throws Exception {
        SecUser secUser = secUser();
        UUID locationId = UUID.randomUUID();

        mockMvc.perform(delete("/locations/{id}/favorite", locationId)
                        .with(user(secUser))
                        .with(csrf())
                        .param("sort", "date")
                        .param("page", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?sort=date&page=0"));

        verify(locationService).removeFromFavorite(locationId, secUser.getId());
    }

    @Test
    void home_withDuplicateLocations_executesMapMergeFunction() throws Exception {
        SecUser secUser = secUser();
        LocationDto location = locationDto();
        WeatherDto weather = weatherDto();

        when(locationService.getByUserIdSorted(eq(secUser.getId()), anyString(), any(Pageable.class)))
                .thenReturn(pageOf(List.of(location, location), PageRequest.of(0, 12)));
        when(weatherApiService.getWeatherByLocation(location)).thenReturn(weather);

        mockMvc.perform(get("/").with(user(secUser)))
                .andExpect(status().isOk());
    }

    @Test
    void home_completionExceptionWithoutCause_usesDirectExceptionMessage() throws Exception {
        SecUser secUser = secUser();
        LocationDto location = locationDto();

        when(locationService.getByUserIdSorted(eq(secUser.getId()), anyString(), any(Pageable.class)))
                .thenReturn(pageOf(List.of(location), PageRequest.of(0, 12)));

        when(weatherApiService.getWeatherByLocation(location))
                .thenThrow(new java.util.concurrent.CompletionException("Direct completion error", null));

        mockMvc.perform(get("/").with(user(secUser)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorMessages"));
    }

}
