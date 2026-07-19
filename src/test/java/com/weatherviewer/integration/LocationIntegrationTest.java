package com.weatherviewer.integration;

import com.weatherviewer.model.Location;
import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.repository.LocationRepository;
import com.weatherviewer.repository.UserRepository;
import com.weatherviewer.security.SecUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LocationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

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
            UnitSystem.METRIC
        );
    }

    @AfterEach
    void cleanUp() {
        locationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void addLocation_validData_savesInDatabase() throws Exception {
        mockMvc.perform(post("/search/add")
                        .with(csrf())
                        .with(user(secUser))
                        .param("name", "Kyiv")
                        .param("latitude", "50.45")
                        .param("longitude", "30.52"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        List<Location> locations = locationRepository.findByUserId(savedUser.getId());
        assertThat(locations).hasSize(1);
        assertThat(locations.get(0).getName()).isEqualTo("Kyiv");
    }

    @Test
    void addLocation_duplicateCoordinates_rejectedByValidator() throws Exception {
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
                .andExpect(status().is3xxRedirection());

        List<Location> locations = locationRepository.findByUserId(savedUser.getId());
        assertThat(locations).hasSize(1);
    }

    @Test
    void deleteLocation_removesFromDatabase() throws Exception {
        Location location = locationRepository.save(new Location()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUser(savedUser));

        mockMvc.perform(delete("/locations/{id}", location.getId())
                        .with(csrf())
                        .with(user(secUser)))
                .andExpect(status().is3xxRedirection());

        assertThat(locationRepository.findById(location.getId())).isEmpty();
    }

    @Test
    void addToFavorite_setsFavoriteTrueInDatabase() throws Exception {
        Location location = locationRepository.save(new Location()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUser(savedUser)
                .setFavorite(false));

        mockMvc.perform(post("/locations/{id}/favorite", location.getId())
                        .with(csrf())
                        .with(user(secUser)))
                .andExpect(status().is3xxRedirection());

        Location updated = locationRepository.findById(location.getId()).orElseThrow();
        assertThat(updated.isFavorite()).isTrue();
    }

    @Test
    void removeFromFavorite_setsFavoriteFalseInDatabase() throws Exception {
        Location location = locationRepository.save(new Location()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUser(savedUser)
                .setFavorite(true));

        mockMvc.perform(delete("/locations/{id}/favorite", location.getId())
                        .with(csrf())
                        .with(user(secUser)))
                .andExpect(status().is3xxRedirection());

        Location updated = locationRepository.findById(location.getId()).orElseThrow();
        assertThat(updated.isFavorite()).isFalse();
    }

    @Test
    void addToFavorite_differentUser_redirectsWithErrorMessage() throws Exception {
        User otherUser = userRepository.save(new User()
                .setEmail("other@example.com")
                .setFirstName("Other")
                .setLastName("User")
                .setPassword(passwordEncoder.encode("Secure1@"))
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER));

        Location location = locationRepository.save(new Location()
                .setName("Kyiv")
                .setLatitude(50.45)
                .setLongitude(30.52)
                .setUser(otherUser)
                .setFavorite(false));

        mockMvc.perform(post("/locations/{id}/favorite", location.getId())
                        .with(csrf())
                        .with(user(secUser)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", "You don't have permission to do that"));

        Location unchanged = locationRepository.findById(location.getId()).orElseThrow();
        assertThat(unchanged.isFavorite()).isFalse();

        userRepository.delete(otherUser);
    }

}
