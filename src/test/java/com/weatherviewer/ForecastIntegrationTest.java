package com.weatherviewer;

import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
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

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ForecastIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private SecUser secUser;

    @BeforeEach
    void setUp() {
        User savedUser = userRepository.save(new User()
                .setEmail("forecast@example.com")
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
                savedUser.getFullName()
        );
    }

    @AfterEach
    void cleanUp() {
        locationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void forecast_invalidLatitude_redirectsWithErrorMessage() throws Exception {
        mockMvc.perform(get("/forecast")
                        .with(user(secUser))
                        .param("lat", "91.0")
                        .param("lon", "30.52"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void forecast_invalidLongitude_redirectsWithErrorMessage() throws Exception {
        mockMvc.perform(get("/forecast")
                        .with(user(secUser))
                        .param("lat", "50.45")
                        .param("lon", "181.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void forecast_locationNotFound_redirectsWithErrorMessage() throws Exception {
        mockMvc.perform(get("/forecast")
                        .with(user(secUser))
                        .param("lat", "50.45")
                        .param("lon", "30.52"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("errorMessage", containsString("not found")));
    }

    @Test
    void forecast_unauthenticated_redirectsToSignIn() throws Exception {
        mockMvc.perform(get("/forecast")
                        .param("lat", "50.45")
                        .param("lon", "30.52"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sign-in"));
    }

}
