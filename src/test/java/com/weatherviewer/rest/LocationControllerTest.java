package com.weatherviewer.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.AddLocationDto;
import com.weatherviewer.dto.LocationDto;
import com.weatherviewer.exception.notfound.LocationNotFoundException;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
@WithMockUser
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LocationService locationService;

    private SecUser mockSecUser;
    private UUID mockUserId;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        mockSecUser = new SecUser(
                mockUserId,
                "john@example.com",
                "hashed",
                Set.of(),
                true,
                "John Doe"
        );
    }

    @Test
    void getMyLocations_returns200AndList() throws Exception {
        List<LocationDto> dtos = List.of(new LocationDto().setName("Kyiv"));
        when(locationService.getByUserId(mockUserId)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/locations/my")
                        .with(user(mockSecUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Kyiv"));
    }

    @Test
    void deleteMyLocation_returns204() throws Exception {
        UUID locationId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/locations/my/{id}", locationId)
                        .with(csrf())
                        .with(user(mockSecUser)))
                .andExpect(status().isNoContent());

        verify(locationService).deleteByIdAndUserId(locationId, mockUserId);
    }

    @Test
    void addMyLocationToFavorite_returns204() throws Exception {
        UUID locationId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/locations/my/{id}/favorite", locationId)
                        .with(csrf())
                        .with(user(mockSecUser)))
                .andExpect(status().isNoContent());

        verify(locationService).addToFavorite(locationId, mockUserId);
    }

    @Test
    void removeMyLocationFromFavorite_returns204() throws Exception {
        UUID locationId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/locations/my/{id}/favorite", locationId)
                        .with(csrf())
                        .with(user(mockSecUser)))
                .andExpect(status().isNoContent());

        verify(locationService).removeFromFavorite(locationId, mockUserId);
    }

    @Test
    void addLocation_returns201AndId() throws Exception {
        UUID id = UUID.randomUUID();
        AddLocationDto dto = new AddLocationDto()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUserId(UUID.randomUUID());

        when(locationService.add(any())).thenReturn(id);

        mockMvc.perform(post("/api/v1/locations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string(("\"" + id + "\"")));
    }

    @Test
    void getLocations_returns200AndList() throws Exception {
        List<LocationDto> dtos = List.of(new LocationDto().setName("Kyiv"));
        when(locationService.getLocations()).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Kyiv"));
    }

    @Test
    void getLocationById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        LocationDto dto = new LocationDto().setName("Kyiv");
        when(locationService.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/locations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Kyiv"));
    }

    @Test
    void getLocationById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(locationService.getById(id)).thenThrow(new LocationNotFoundException("Location not found by id: " + id));

        mockMvc.perform(get("/api/v1/locations/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLocationsByUserId_returns200AndList() throws Exception {
        UUID userId = UUID.randomUUID();
        List<LocationDto> dtos = List.of(new LocationDto().setName("Kyiv"));
        when(locationService.getByUserId(userId)).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/locations/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Kyiv"));
    }

    @Test
    void deleteLocationById_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/locations/{id}", id)
                        .param("userId", userId.toString())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(locationService).deleteByIdAndUserId(id, userId);
    }

    @Test
    void deleteLocationsByUserId_returns204() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/locations/user/{id}", userId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(locationService).deleteByUserId(userId);
    }

    @Test
    void deleteLocationByNameAndUserId_returns204() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/locations/{name}/user/{id}", "Kyiv", userId).with(csrf()))
                .andExpect(status().isNoContent());

        verify(locationService).deleteByNameAndUserId("Kyiv", userId);
    }

    @Test
    void existsByNameAndUserId_returnsTrue() throws Exception {
        UUID userId = UUID.randomUUID();
        when(locationService.existsByNameAndUserId("Kyiv", userId)).thenReturn(true);

        mockMvc.perform(get("/api/v1/locations/exists/name/{name}/user/{userId}", "Kyiv", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void existsByNameAndUserId_returnsFalse() throws Exception {
        UUID userId = UUID.randomUUID();
        when(locationService.existsByNameAndUserId("Kyiv", userId)).thenReturn(false);

        mockMvc.perform(get("/api/v1/locations/exists/name/{name}/user/{userId}", "Kyiv", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void existsByCoordinatesAndUserId_returnsTrue() throws Exception {
        UUID userId = UUID.randomUUID();
        when(locationService.existsByCoordinatesAndUserId(50.45, 30.52, userId)).thenReturn(true);

        mockMvc.perform(get("/api/v1/locations/exists/coordinates/user/{userId}", userId)
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void addToFavorite_returns204() throws Exception {
        UUID locationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/locations/{id}/favorite", locationId)
                        .with(csrf())
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());

        verify(locationService).addToFavorite(locationId, userId);
    }

    @Test
    void removeFromFavorite_returns204() throws Exception {
        UUID locationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/locations/{id}/favorite", locationId)
                        .with(csrf())
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());

        verify(locationService).removeFromFavorite(locationId, userId);
    }

    @Test
    void getSortedLocations_defaultSort_returnsByDate() throws Exception {
        UUID userId = UUID.randomUUID();
        when(locationService.getByUserIdSortedByDate(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/locations/user/{userId}/locations", userId))
                .andExpect(status().isOk());

        verify(locationService).getByUserIdSortedByDate(userId);
    }

    @Test
    void getSortedLocations_nameAsc_returnsByNameAsc() throws Exception {
        UUID userId = UUID.randomUUID();
        when(locationService.getByUserIdSortedByNameAsc(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/locations/user/{userId}/locations", userId)
                        .param("sort", "nameAsc"))
                .andExpect(status().isOk());

        verify(locationService).getByUserIdSortedByNameAsc(userId);
    }

    @Test
    void getSortedLocations_nameDesc_returnsByNameDesc() throws Exception {
        UUID userId = UUID.randomUUID();
        when(locationService.getByUserIdSortedByNameDesc(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/locations/user/{userId}/locations", userId)
                        .param("sort", "nameDesc"))
                .andExpect(status().isOk());

        verify(locationService).getByUserIdSortedByNameDesc(userId);
    }

    @Test
    void getSortedLocations_favoriteFirst_returnsByFavorite() throws Exception {
        UUID userId = UUID.randomUUID();
        when(locationService.getByUserIdSortedByFavorite(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/locations/user/{userId}/locations", userId)
                        .param("sort", "favoriteFirst"))
                .andExpect(status().isOk());

        verify(locationService).getByUserIdSortedByFavorite(userId);
    }

    @Test
    void getSortedLocations_favoritesOnly_returnsFavorites() throws Exception {
        UUID userId = UUID.randomUUID();
        when(locationService.getFavoritesByUserId(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/locations/user/{userId}/locations", userId)
                        .param("sort", "favoritesOnly"))
                .andExpect(status().isOk());

        verify(locationService).getFavoritesByUserId(userId);
    }

}
