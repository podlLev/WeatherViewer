package com.weatherviewer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.ConcurrentModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MapController.class)
class MapControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LocationService locationService;

    @MockitoSpyBean
    ObjectMapper objectMapper;

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

    @Test
    void map_returns200AndViewWithLocations() throws Exception {
        SecUser user = secUser();
        when(locationService.getByUserId(user.getId())).thenReturn(List.of(locationDto()));

        mockMvc.perform(get("/map").with(user(user)))
                .andExpect(status().isOk())
                .andExpect(view().name("map"))
                .andExpect(model().attributeExists("locations"))
                .andExpect(model().attribute("login", "John Doe"));
    }

    @Test
    void map_noSavedLocations_stillReturns200WithEmptyList() throws Exception {
        SecUser user = secUser();
        when(locationService.getByUserId(user.getId())).thenReturn(List.of());

        mockMvc.perform(get("/map").with(user(user)))
                .andExpect(status().isOk())
                .andExpect(view().name("map"))
                .andExpect(model().attribute("locations", List.of()));
    }

    @Test
    void map_savedLocation_mapDataJsonIncludesLocationAndLabels() throws Exception {
        SecUser user = secUser();
        when(locationService.getByUserId(user.getId())).thenReturn(List.of(locationDto()));

        MvcResult result = mockMvc.perform(get("/map").with(user(user)))
                .andExpect(status().isOk())
                .andReturn();

        String mapDataJson = (String) Objects.requireNonNull(result.getModelAndView()).getModel().get("mapDataJson");
        assertThat(mapDataJson).contains("\"name\":\"Kyiv\"");
        assertThat(mapDataJson).contains("\"forecast\"");
        assertThat(mapDataJson).contains("\"addLocation\"");
        assertThat(mapDataJson).contains("\"locationNamePlaceholder\"");
        assertThat(mapDataJson).contains("\"addLocationUrl\":\"/search/add\"");
    }

    @Test
    void map_jsonSerializationFails_fallsBackToEmptyMapDataJson() throws Exception {
        SecUser user = secUser();
        when(locationService.getByUserId(user.getId())).thenReturn(List.of(locationDto()));
        doThrow(new JsonProcessingException("boom") {
        }).when(objectMapper).writeValueAsString(any());

        mockMvc.perform(get("/map").with(user(user)))
                .andExpect(status().isOk())
                .andExpect(view().name("map"))
                .andExpect(model().attribute("mapDataJson", "{\"locations\":[],\"layerLabels\":{}}"));
    }

    @Test
    void map_whenCsrfTokenPresent_includesCsrfInMapDataJson() throws Exception {
        SecUser user = secUser();
        when(locationService.getByUserId(user.getId())).thenReturn(List.of(locationDto()));

        MvcResult result = mockMvc.perform(get("/map")
                        .with(user(user))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        String mapDataJson = (String) Objects.requireNonNull(result.getModelAndView()).getModel().get("mapDataJson");
        assertThat(mapDataJson).contains("\"csrfParam\":\"_csrf\"");
        assertThat(mapDataJson).contains("\"csrfToken\":");
    }

}
