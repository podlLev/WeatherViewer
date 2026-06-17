package com.weatherviewer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void signInPage_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/sign-in"))
                .andExpect(status().isOk());
    }

    @Test
    void signUpPage_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk());
    }

    @Test
    void signInFailurePage_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/sign-in-failure"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void home_unauthenticated_redirectsToSignIn() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sign-in"));
    }

    @Test
    void profile_unauthenticated_redirectsToSignIn() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sign-in"));
    }

    @Test
    void search_unauthenticated_redirectsToSignIn() throws Exception {
        mockMvc.perform(get("/search").param("q", "Kyiv"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sign-in"));
    }

    @Test
    void forecast_unauthenticated_redirectsToSignIn() throws Exception {
        mockMvc.perform(get("/forecast")
                        .param("lat", "50.45")
                        .param("lon", "30.52"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sign-in"));
    }

    @Test
    void apiUsers_unauthenticated_redirectsToSignIn() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sign-in"));
    }

    @Test
    void apiLocations_unauthenticated_redirectsToSignIn() throws Exception {
        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/sign-in"));
    }

    @Test
    @WithMockUser
    void apiUsers_authenticated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "users:read")
    void createUser_withoutWriteAuthority_returnsUnprocessableEntity() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(authorities = "users:read")
    void createUser_withoutWriteAuthority_returnsForbidden() throws Exception {
        String validBody = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john@example.com",
                "password": "Secure1@",
                "repeatPassword": "Secure1@"
            }
            """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType("application/json")
                        .content(validBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "users:write")
    void createUser_withWriteAuthority_doesNotReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status).isNotEqualTo(403);
                });
    }

    @Test
    @WithMockUser
    void logout_redirectsToSignIn() throws Exception {
        mockMvc.perform(post("/sign-out"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));
    }

}
