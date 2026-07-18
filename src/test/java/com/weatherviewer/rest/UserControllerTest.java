package com.weatherviewer.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherviewer.dto.CreateUserDto;
import com.weatherviewer.dto.UpdateUserDto;
import com.weatherviewer.dto.UpdateUserRoleDto;
import com.weatherviewer.dto.UserDto;
import com.weatherviewer.dto.helper.ValidatorTestFactory;
import com.weatherviewer.exception.notfound.UserNotFoundException;
import com.weatherviewer.service.UserService;
import com.weatherviewer.validation.validator.UniqueEmailValidator;
import com.weatherviewer.validation.validator.UniqueLocationValidator;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {}

    @TestConfiguration
    static class ValidatorConfig {

        @Bean
        @Primary
        public Validator validator() {
            return ValidatorTestFactory.skipValidator(
                    UniqueEmailValidator.class,
                    UniqueLocationValidator.class
            );
        }
    }

    @Test
    @WithMockUser(authorities = "users:write")
    void createUser_returns201AndId() throws Exception {
        UUID id = UUID.randomUUID();
        CreateUserDto dto = new CreateUserDto()
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john@example.com")
                .setPassword("Secure1@")
                .setRepeatPassword("Secure1@");

        when(userService.create(any())).thenReturn(id);

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("\"" + id + "\""));
    }

    @Test
    @WithMockUser
    void createUser_withoutWriteAuthority_returns403() throws Exception {
        CreateUserDto dto = new CreateUserDto()
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john@example.com")
                .setPassword("Secure1@")
                .setRepeatPassword("Secure1@");

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "users:write")
    void getUsers_returns200AndPage() throws Exception {
        List<UserDto> dtos = List.of(new UserDto().setEmail("john@example.com"));
        when(userService.getUsers(any(Pageable.class))).thenReturn(new PageImpl<>(dtos));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("john@example.com"));
    }

    @Test
    @WithMockUser(authorities = "users:write")
    void getUserById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UserDto dto = new UserDto().setEmail("john@example.com");
        when(userService.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser(authorities = "users:write")
    void getUserById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getById(id)).thenThrow(new UserNotFoundException("User not found by id: " + id));

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "users:write")
    void getUserByEmail_returns200() throws Exception {
        UserDto dto = new UserDto().setEmail("john@example.com");
        when(userService.getByEmail("john@example.com")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/email")
                        .param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser(authorities = "users:write")
    void getUserByEmail_invalidEmail_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/users/email")
                        .param("email", "not-an-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "users:write")
    void updateUser_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateUserDto dto = new UpdateUserDto()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe");
        UserDto userDto = new UserDto().setEmail("john@example.com");

        when(userService.update(eq(id), any())).thenReturn(userDto);

        mockMvc.perform(put("/api/v1/users/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @WithMockUser
    void updateUser_withoutWriteAuthority_returns403() throws Exception {
        UUID id = UUID.randomUUID();
        UpdateUserDto dto = new UpdateUserDto()
                .setEmail("john@example.com")
                .setFirstName("John")
                .setLastName("Doe");

        mockMvc.perform(put("/api/v1/users/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "users:write")
    void deleteUserById_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).delete(id);
    }

    @Test
    @WithMockUser
    void deleteUserById_withoutWriteAuthority_returns403() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", id).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "users:write")
    void updateUserRole_returns200() throws Exception {
        UpdateUserRoleDto dto = new UpdateUserRoleDto()
                .setUserId(UUID.randomUUID())
                .setNewRole("ADMIN");

        mockMvc.perform(put("/api/v1/users/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(userService).updateUserRole(any());
    }

    @Test
    @WithMockUser
    void updateUserRole_withoutWriteAuthority_returns403() throws Exception {
        UpdateUserRoleDto dto = new UpdateUserRoleDto()
                .setUserId(UUID.randomUUID())
                .setNewRole("ADMIN");

        mockMvc.perform(put("/api/v1/users/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

}
