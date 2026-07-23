package com.weatherviewer.controller;

import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.repository.LocationRepository;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import com.weatherviewer.service.WeatherApiService;
import com.weatherviewer.validation.validator.PasswordMatchesValidator;
import com.weatherviewer.validation.validator.UniqueEmailValidator;
import com.weatherviewer.validation.validator.UniqueLocationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WeatherApiService weatherApiService;

    @MockitoBean
    LocationService locationService;

    @MockitoBean
    LocationRepository locationRepository;

    @MockitoBean
    UniqueEmailValidator uniqueEmailValidator;

    @MockitoBean
    UniqueLocationValidator uniqueLocationValidator;

    @MockitoBean
    PasswordMatchesValidator passwordMatchesValidator;

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

    @BeforeEach
    void setUp() {
        when(uniqueEmailValidator.isValid(any(), any())).thenReturn(true);
        when(passwordMatchesValidator.isValid(any(), any())).thenReturn(true);
        when(uniqueLocationValidator.isValid(any(), any())).thenReturn(true);
        when(locationRepository.existsByLatitudeAndLongitudeAndUserId(anyDouble(), anyDouble(), any()))
                .thenReturn(false);
    }

    @Test
    void search_returns200AndView() throws Exception {
        SecUser user = secUser();
        List<GeoLocationDto> results = List.of(
                new GeoLocationDto().setName("Kyiv"),
                new GeoLocationDto().setName("Kyiv Oblast")
        );
        when(weatherApiService.getCitiesByName("Kyiv")).thenReturn(results);

        mockMvc.perform(get("/search")
                        .with(user(user))
                        .param("q", "Kyiv"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("foundLocations", "addLocation", "login"))
                .andExpect(model().attribute("login", "John Doe"));
    }

    @Test
    void search_returnsFoundLocations() throws Exception {
        SecUser user = secUser();
        List<GeoLocationDto> results = List.of(new GeoLocationDto().setName("Kyiv"));
        when(weatherApiService.getCitiesByName("Kyiv")).thenReturn(results);

        mockMvc.perform(get("/search")
                        .with(user(user))
                        .param("q", "Kyiv"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("foundLocations", results));
    }

    @Test
    void search_emptyResults_returnsEmptyList() throws Exception {
        SecUser user = secUser();
        when(weatherApiService.getCitiesByName("unknown")).thenReturn(List.of());

        mockMvc.perform(get("/search")
                        .with(user(user))
                        .param("q", "unknown"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("foundLocations", List.of()));
    }

    @Test
    void addLocation_success_redirectsToHome() throws Exception {
        SecUser user = secUser();

        mockMvc.perform(post("/search/add")
                        .with(user(user))
                        .with(csrf())
                        .param("name", "Kyiv")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52")
                        .param("userId", UUID.randomUUID().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(locationService).add(any(AddLocationDto.class));
    }

    @Test
    void addLocation_missingName_redirectsToHomeWithErrors() throws Exception {
        SecUser user = secUser();

        mockMvc.perform(post("/search/add")
                        .with(user(user))
                        .with(csrf())
                        .param("name", "")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52")
                        .param("userId", UUID.randomUUID().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(locationService, never()).add(any());
    }

}
