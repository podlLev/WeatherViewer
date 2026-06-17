package com.weatherviewer.integration;

import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SignInIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.save(new User()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword(passwordEncoder.encode("Secure1@"))
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER));
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void signIn_validCredentials_authenticatesAndRedirectsHome() throws Exception {
        mockMvc.perform(formLogin("/sign-in")
                        .user("email", "john@example.com")
                        .password("password", "Secure1@"))
                .andExpect(authenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void signIn_invalidPassword_redirectsToSignInFailure() throws Exception {
        mockMvc.perform(formLogin("/sign-in")
                        .user("email", "john@example.com")
                        .password("password", "WrongPassword1@"))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in-failure"));
    }

    @Test
    void signIn_nonExistentEmail_redirectsToSignInFailure() throws Exception {
        mockMvc.perform(formLogin("/sign-in")
                        .user("email", "nobody@example.com")
                        .password("password", "Secure1@"))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in-failure"));
    }

    @Test
    void logout_clearsAuthenticationAndRedirectsToSignIn() throws Exception {
        mockMvc.perform(logout("/sign-out"))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));
    }

}
