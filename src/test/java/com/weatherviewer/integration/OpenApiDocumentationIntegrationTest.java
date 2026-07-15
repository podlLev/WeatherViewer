package com.weatherviewer.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void apiDocsAreServedAndWellFormed() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.openapi").value("3.0.1"))
                .andExpect(jsonPath("$.info.title").value("WeatherViewer API"))
                .andExpect(jsonPath("$.paths['/api/v1/weather/city']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/locations/my']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/users']").exists())
                .andExpect(jsonPath("$.components.schemas.WeatherDto").exists())
                .andExpect(jsonPath("$.components.securitySchemes.sessionCookieAuth").exists());
    }

    @Test
    void swaggerUiIsReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void scalarDocsPageIsReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/scalar"))
                .andExpect(status().isOk());
    }

}
