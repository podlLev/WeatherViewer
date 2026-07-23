package com.weatherviewer.integration;

import com.weatherviewer.dto.GeoLocationDto;
import com.weatherviewer.model.Location;
import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.repository.LocationRepository;
import com.weatherviewer.repository.UserRepository;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.WeatherApiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SearchIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoBean
    WeatherApiService weatherApiService;

    private User savedUser;
    private SecUser secUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(new User()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword(passwordEncoder.encode("Secure1@"))
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER));

        secUser = new SecUser(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getPassword(),
                Set.of(),
                true,
                savedUser.getFullName(),
                UnitSystem.METRIC,
                null
        );
    }

    @AfterEach
    void cleanUp() {
        locationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void search_returnsViewWithGeocodingResults() throws Exception {
        List<GeoLocationDto> results = List.of(
                new GeoLocationDto().setName("Kyiv").setLatitude(50.45).setLongitude(30.52),
                new GeoLocationDto().setName("Kyiv Oblast").setLatitude(50.0).setLongitude(31.0)
        );
        when(weatherApiService.getCitiesByName("Kyiv")).thenReturn(results);

        mockMvc.perform(get("/search")
                        .with(csrf())
                        .with(user(secUser))
                        .param("q", "Kyiv"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attribute("foundLocations", results))
                .andExpect(model().attribute("login", "John Doe"));
    }

    @Test
    void addLocation_validData_savesInDatabaseViaRealValidator() throws Exception {
        mockMvc.perform(post("/search/add")
                        .with(csrf())
                        .with(user(secUser))
                        .param("name", "Kyiv")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("successMessage", "Location added successfully!"));

        List<Location> locations = locationRepository.findByUserId(savedUser.getId());
        assertThat(locations).hasSize(1);
        assertThat(locations.get(0).getName()).isEqualTo("Kyiv");
    }

    @Test
    void addLocation_duplicateCoordinates_rejectedByRealUniqueLocationValidator() throws Exception {
        mockMvc.perform(post("/search/add")
                        .with(csrf())
                        .with(user(secUser))
                        .param("name", "Kyiv")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/search/add")
                        .with(csrf())
                        .with(user(secUser))
                        .param("name", "Kyiv Duplicate")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessages"));

        List<Location> locations = locationRepository.findByUserId(savedUser.getId());
        assertThat(locations).hasSize(1);
    }

    @Test
    void addLocation_blankName_rejectedAndNotSaved() throws Exception {
        mockMvc.perform(post("/search/add")
                        .with(csrf())
                        .with(user(secUser))
                        .param("name", "")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessages"));

        assertThat(locationRepository.findByUserId(savedUser.getId())).isEmpty();
    }

    @Test
    void addLocation_invalidLatitude_rejectedAndNotSaved() throws Exception {
        mockMvc.perform(post("/search/add")
                        .with(csrf())
                        .with(user(secUser))
                        .param("name", "Invalid")
                        .param("latitude", "91.0")
                        .param("longitude", "30.52"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessages"));

        assertThat(locationRepository.findByUserId(savedUser.getId())).isEmpty();
    }

    @Test
    void addLocation_sameCoordinatesDifferentUser_bothSaved() throws Exception {
        User otherUser = userRepository.save(new User()
                .setEmail("other@example.com")
                .setFirstName("Other")
                .setLastName("User")
                .setPassword(passwordEncoder.encode("Secure1@"))
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER));
        SecUser otherSecUser = new SecUser(
                otherUser.getId(), otherUser.getEmail(), otherUser.getPassword(),
                Set.of(), true, otherUser.getFullName(),
                UnitSystem.METRIC, null
        );

        mockMvc.perform(post("/search/add")
                        .with(csrf())
                        .with(user(secUser))
                        .param("name", "Kyiv")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/search/add")
                        .with(csrf())
                        .with(user(otherSecUser))
                        .param("name", "Kyiv")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Location added successfully!"));

        assertThat(locationRepository.findByUserId(savedUser.getId())).hasSize(1);
        assertThat(locationRepository.findByUserId(otherUser.getId())).hasSize(1);

        userRepository.delete(otherUser);
    }

}
