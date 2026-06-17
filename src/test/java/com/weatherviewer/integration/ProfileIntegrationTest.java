package com.weatherviewer.integration;

import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

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
                savedUser.getFullName()
        );
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void updateProfile_validData_updatesInDatabase() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(user(secUser))
                        .param("email", "john@example.com")
                        .param("firstName", "Jane")
                        .param("lastName", "Smith"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        User updated = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updated.getFirstName()).isEqualTo("Jane");
        assertThat(updated.getLastName()).isEqualTo("Smith");
    }

    @Test
    void updateProfile_newPassword_hashesAndUpdates() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(user(secUser))
                        .param("email", "john@example.com")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("password", "NewSecure1@"))
                .andExpect(status().is3xxRedirection());

        User updated = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updated.getPassword()).isNotEqualTo("NewSecure1@");
        assertThat(passwordEncoder.matches("NewSecure1@", updated.getPassword())).isTrue();
    }

    @Test
    void updateProfile_emailTakenByAnotherUser_doesNotUpdate() throws Exception {
        userRepository.save(new User()
                .setEmail("taken@example.com")
                .setFirstName("Other")
                .setLastName("User")
                .setPassword(passwordEncoder.encode("Secure1@"))
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER));

        mockMvc.perform(post("/profile")
                        .with(user(secUser))
                        .param("email", "taken@example.com")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk());

        User unchanged = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(unchanged.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void updateProfile_blankFirstName_doesNotUpdate() throws Exception {
        mockMvc.perform(post("/profile")
                        .with(user(secUser))
                        .param("email", "john@example.com")
                        .param("firstName", "")
                        .param("lastName", "Doe"))
                .andExpect(status().isOk());

        User unchanged = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(unchanged.getFirstName()).isEqualTo("John");
    }

}
