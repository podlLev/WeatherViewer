package com.weatherviewer.controller;

import com.weatherviewer.model.User;
import com.weatherviewer.model.enums.Role;
import com.weatherviewer.model.enums.UnitSystem;
import com.weatherviewer.model.enums.UserStatus;
import com.weatherviewer.security.SecUser;
import com.weatherviewer.service.UserService;
import com.weatherviewer.validation.validator.PasswordMatchesValidator;
import com.weatherviewer.validation.validator.UniqueEmailValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    UniqueEmailValidator uniqueEmailValidator;

    @MockitoBean
    PasswordMatchesValidator passwordMatchesValidator;

    private SecUser secUser() {
        return new SecUser(
                UUID.randomUUID(),
                "john@example.com",
                "hashed",
                Set.of(),
                true,
                "John Doe",
                UnitSystem.METRIC
        );
    }

    private User userEntity(UUID id) {
        return (User) new User()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("hashed")
                .setStatus(UserStatus.ACTIVE)
                .setRole(Role.USER)
                .setId(id);
    }

    @BeforeEach
    void setUp() {
        when(uniqueEmailValidator.isValid(any(), any())).thenReturn(true);
        when(passwordMatchesValidator.isValid(any(), any())).thenReturn(true);
    }

    @Test
    void getProfile_returns200AndView() throws Exception {
        SecUser user = secUser();
        when(userService.getEntityById(user.getId())).thenReturn(userEntity(user.getId()));

        mockMvc.perform(get("/profile").with(user(user)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("login", "user"));
    }

    @Test
    void getProfile_addsFullNameToModel() throws Exception {
        SecUser user = secUser();
        when(userService.getEntityById(user.getId())).thenReturn(userEntity(user.getId()));

        mockMvc.perform(get("/profile").with(user(user)))
                .andExpect(model().attribute("login", "John Doe"));
    }

    @Test
    void updateProfile_success_redirectsToProfile() throws Exception {
        SecUser user = secUser();
        when(userService.getEntityById(user.getId())).thenReturn(userEntity(user.getId()));

        mockMvc.perform(post("/profile")
                        .with(user(user))
                        .with(csrf())
                        .param("email", "john@example.com")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(userService).update(eq(user.getId()), any());
    }

    @Test
    void updateProfile_validationErrors_returnsProfileView() throws Exception {
        SecUser user = secUser();
        when(userService.getEntityById(user.getId())).thenReturn(userEntity(user.getId()));

        mockMvc.perform(post("/profile")
                        .with(user(user))
                        .with(csrf())
                        .param("email", "")
                        .param("firstName", "")
                        .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"));

        verify(userService, never()).update(any(), any());
    }

    @Test
    void updateProfile_validationErrors_addsLoginToModel() throws Exception {
        SecUser user = secUser();
        when(userService.getEntityById(user.getId())).thenReturn(userEntity(user.getId()));

        mockMvc.perform(post("/profile")
                        .with(user(user))
                        .with(csrf())
                        .param("email", "")
                        .param("firstName", "")
                        .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("login"));
    }

    @Test
    void getProfile_unauthenticated_redirects() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sign-in"));
    }

}
