package com.weatherviewer.rest;

import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.integration.MapTileLayer;
import com.weatherviewer.service.integration.WeatherTileClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MapTileController.class)
class MapTileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WeatherTileClient weatherTileClient;

    private SecUser secUser() {
        return new SecUser(
                UUID.randomUUID(), "john@example.com", "hashed", Set.of(),
                true, "John Doe", UnitSystem.METRIC, null
        );
    }

    @Test
    void tile_knownLayer_returnsPngWithCacheHeaders() throws Exception {
        byte[] fakePng = new byte[]{1, 2, 3};
        when(weatherTileClient.fetchTile(MapTileLayer.PRECIPITATION, 5, 10, 12)).thenReturn(fakePng);

        mockMvc.perform(get("/map/tiles/precipitation/5/10/12").with(user(secUser())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(fakePng))
                .andExpect(header().exists("Cache-Control"));
    }

    @Test
    void tile_layerNameIsCaseInsensitive() throws Exception {
        when(weatherTileClient.fetchTile(MapTileLayer.CLOUDS, 3, 1, 1)).thenReturn(new byte[]{9});

        mockMvc.perform(get("/map/tiles/CLOUDS/3/1/1").with(user(secUser())))
                .andExpect(status().isOk());
    }

    @Test
    void tile_unknownLayer_returns400AndNeverCallsClient() throws Exception {
        mockMvc.perform(get("/map/tiles/not-a-real-layer/5/10/12").with(user(secUser())))
                .andExpect(status().isBadRequest());

        verify(weatherTileClient, never()).fetchTile(eq(MapTileLayer.PRECIPITATION), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void tile_nonNumericCoordinate_returns400() throws Exception {
        mockMvc.perform(get("/map/tiles/precipitation/abc/10/12").with(user(secUser())))
                .andExpect(status().isBadRequest());
    }

}
