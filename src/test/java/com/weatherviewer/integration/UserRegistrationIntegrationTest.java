package com.weatherviewer.integration;

import com.weatherviewer.model.User;
import com.weatherviewer.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void signUp_validData_createsUserInDatabase() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Secure1@"))
                .andExpect(status().is3xxRedirection());

        Optional<User> savedUser = userRepository.findByEmail("john@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void signUp_passwordIsHashed_notStoredAsPlainText() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Secure1@"))
                .andExpect(status().is3xxRedirection());

        User savedUser = userRepository.findByEmail("john@example.com").orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo("Secure1@");
    }

    @Test
    void signUp_duplicateEmail_doesNotCreateSecondUser() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Secure1@"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("firstName", "Jane")
                        .param("lastName", "Smith")
                        .param("email", "john@example.com")
                        .param("password", "Secure2@")
                        .param("repeatPassword", "Secure2@"))
                .andExpect(status().isOk());

        assertThat(userRepository.findByEmail("john@example.com").get().getFirstName())
                .isEqualTo("John");
    }

    @Test
    void signUp_passwordsDoNotMatch_doesNotCreateUser() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "mismatch@example.com")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Different1@"))
                .andExpect(status().isOk());

        assertThat(userRepository.findByEmail("mismatch@example.com")).isEmpty();
    }

    @Test
    void signUp_newUserHasDefaultRoleAndStatus() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "defaults@example.com")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Secure1@"))
                .andExpect(status().is3xxRedirection());

        User savedUser = userRepository.findByEmail("defaults@example.com").orElseThrow();
        assertThat(savedUser.getRole().name()).isEqualTo("USER");
        assertThat(savedUser.getStatus().name()).isEqualTo("PENDING");
    }

}
