package com.weatherviewer.controller;

import com.weatherviewer.exception.InvalidTokenException;
import com.weatherviewer.service.VerificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PasswordResetController.class)
class PasswordResetControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    VerificationService verificationService;

    @Test
    @WithMockUser
    void forgotPassword_returns200AndView() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot-password"))
                .andExpect(model().attributeExists("forgotPasswordDto"));
    }

    @Test
    @WithMockUser
    void processForgotPassword_validationErrors_returnsFormView() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .with(csrf())
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot-password"));

        verify(verificationService, never()).requestPasswordReset(anyString());
    }

    @Test
    @WithMockUser
    void processForgotPassword_invalidEmailFormat_returnsFormView() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .with(csrf())
                        .param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot-password"));

        verify(verificationService, never()).requestPasswordReset(anyString());
    }

    @Test
    @WithMockUser
    void processForgotPassword_success_redirectsToSignInWithGenericMessage() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .with(csrf())
                        .param("email", "john@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));

        verify(verificationService).requestPasswordReset("john@example.com");
    }

    @Test
    @WithMockUser
    void processForgotPassword_unregisteredEmail_stillShowsGenericSuccessRedirect() throws Exception {
        doNothing().when(verificationService).requestPasswordReset("nobody@example.com");

        mockMvc.perform(post("/forgot-password")
                        .with(csrf())
                        .param("email", "nobody@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));

        verify(verificationService).requestPasswordReset("nobody@example.com");
    }

    @Test
    @WithMockUser
    void resetPassword_returns200AndViewWithTokenPrefilled() throws Exception {
        mockMvc.perform(get("/reset-password").param("token", "raw-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attributeExists("resetPasswordDto"));
    }

    @Test
    @WithMockUser
    void processResetPassword_validationErrors_returnsFormView() throws Exception {
        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", "raw-token")
                        .param("password", "")
                        .param("repeatPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"));

        verify(verificationService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    @WithMockUser
    void processResetPassword_passwordsDoNotMatch_returnsFormView() throws Exception {
        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", "raw-token")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Different1@"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"));

        verify(verificationService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    @WithMockUser
    void processResetPassword_invalidToken_returnsFormViewWithError() throws Exception {
        doThrow(new InvalidTokenException("This link has expired or was already used."))
                .when(verificationService).resetPassword("bad-token", "Secure1@");

        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", "bad-token")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Secure1@"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser
    void processResetPassword_success_redirectsToSignIn() throws Exception {
        doNothing().when(verificationService).resetPassword("raw-token", "Secure1@");

        mockMvc.perform(post("/reset-password")
                        .with(csrf())
                        .param("token", "raw-token")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Secure1@"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));

        verify(verificationService).resetPassword("raw-token", "Secure1@");
    }

}
