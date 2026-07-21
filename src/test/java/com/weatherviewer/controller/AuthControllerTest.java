package com.weatherviewer.controller;

import com.weatherviewer.exception.InvalidTokenException;
import com.weatherviewer.exception.notfound.UserNotFoundException;
import com.weatherviewer.model.User;
import com.weatherviewer.repository.UserRepository;
import com.weatherviewer.service.LoginService;
import com.weatherviewer.service.UserService;
import com.weatherviewer.service.VerificationService;
import com.weatherviewer.validation.validator.PasswordMatchesValidator;
import com.weatherviewer.validation.validator.UniqueEmailValidator;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    LoginService loginService;

    @MockitoBean
    VerificationService verificationService;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    UniqueEmailValidator uniqueEmailValidator;

    @MockitoBean
    PasswordMatchesValidator passwordMatchesValidator;

    @BeforeEach
    void setUp() {
        when(uniqueEmailValidator.isValid(any(), any())).thenReturn(true);
        when(passwordMatchesValidator.isValid(any(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser
    void signIn_returns200AndView() throws Exception {
        mockMvc.perform(get("/sign-in"))
                .andExpect(status().isOk())
                .andExpect(view().name("sign-in"))
                .andExpect(model().attributeDoesNotExist("redirect"));
    }

    @Test
    @WithMockUser
    void signIn_withRedirectParam_addsToModel() throws Exception {
        mockMvc.perform(get("/sign-in").param("redirect", "/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("redirect", "/dashboard"));
    }

    @Test
    @WithMockUser
    void signInFailure_redirectsToSignIn() throws Exception {
        mockMvc.perform(get("/sign-in-failure"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));
    }

    @Test
    @WithMockUser
    void signUp_returns200AndView() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("sign-up"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser
    void signUp_withRedirectParam_addsToModel() throws Exception {
        mockMvc.perform(get("/sign-up").param("redirect", "/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("redirect", "/dashboard"));
    }

    @Test
    @WithMockUser
    void processSignUp_validationErrors_returnsSignUpView() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("email", "")
                        .param("firstName", "")
                        .param("lastName", "")
                        .param("password", "")
                        .param("repeatPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("sign-up"));

        verify(userService, never()).create(any());
    }

    @Test
    @WithMockUser
    void processSignUp_success_redirectsToHome() throws Exception {
        doNothing().when(loginService).login(anyString(), anyString());

        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Secure1@"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(loginService).login("john@example.com", "Secure1@");
    }

    @Test
    @WithMockUser
    void processSignUp_success_withRedirect_redirectsToUrl() throws Exception {
        doNothing().when(loginService).login(anyString(), anyString());

        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Secure1@")
                        .param("redirect", "/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(loginService).login("john@example.com", "Secure1@");
    }

    @Test
    @WithMockUser
    void processSignUp_success_withBlankRedirect_redirectsToUrl() throws Exception {
        doNothing().when(loginService).login(anyString(), anyString());

        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Secure1@")
                        .param("redirect", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(loginService).login("john@example.com", "Secure1@");
    }

    @Test
    @WithMockUser
    void processSignUp_autoLoginFails_redirectsToSignIn() throws Exception {
        doThrow(new ServletException("Login failed"))
                .when(loginService).login(anyString(), anyString());

        mockMvc.perform(post("/sign-up")
                        .with(csrf())
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@example.com")
                        .param("password", "Secure1@")
                        .param("repeatPassword", "Secure1@"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"));
    }

    @Test
    @WithMockUser
    void verifyEmail_validToken_verifiesAndRedirectsToSignInWithSuccess() throws Exception {
        doNothing().when(verificationService).confirmEmail("valid-token");

        mockMvc.perform(get("/verify-email").param("token", "valid-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"))
                .andExpect(flash().attribute("successMessage", "Your email has been verified. You can now sign in."));

        verify(verificationService).confirmEmail("valid-token");
    }

    @Test
    @WithMockUser
    void verifyEmail_invalidOrExpiredToken_redirectsToSignInWithError() throws Exception {
        doThrow(new InvalidTokenException("Invalid or expired verification token"))
                .when(verificationService).confirmEmail("invalid-token");

        mockMvc.perform(get("/verify-email").param("token", "invalid-token"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"))
                .andExpect(flash().attribute("errorMessage",
                        "Invalid or expired verification token Please request a new verification email."));

        verify(verificationService).confirmEmail("invalid-token");
    }

    @Test
    @WithMockUser
    void resendVerification_pendingUser_sendsVerificationEmailAndRedirects() throws Exception {
        User pendingUser = new User();
        pendingUser.setStatus(com.weatherviewer.model.enums.UserStatus.PENDING);
        when(userService.getEntityByEmail("john@example.com")).thenReturn(pendingUser);

        mockMvc.perform(post("/resend-verification")
                        .with(csrf())
                        .param("email", "john@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"))
                .andExpect(flash().attribute("successMessage",
                        "If that account needs verifying, we've sent a fresh link to its email address."));

        verify(verificationService).sendVerificationEmail(pendingUser);
    }

    @Test
    @WithMockUser
    void resendVerification_activeUser_doesNotSendEmailAndRedirectsWithSameMessage() throws Exception {
        User activeUser = new User();
        activeUser.setStatus(com.weatherviewer.model.enums.UserStatus.ACTIVE);
        when(userService.getEntityByEmail("active@example.com")).thenReturn(activeUser);

        mockMvc.perform(post("/resend-verification")
                        .with(csrf())
                        .param("email", "active@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"))
                .andExpect(flash().attribute("successMessage",
                        "If that account needs verifying, we've sent a fresh link to its email address."));

        verify(verificationService, never()).sendVerificationEmail(any());
    }

    @Test
    @WithMockUser
    void resendVerification_unknownEmail_doesNotSendEmailAndRedirectsWithSameMessage() throws Exception {
        when(userService.getEntityByEmail("unknown@example.com"))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/resend-verification")
                        .with(csrf())
                        .param("email", "unknown@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in"))
                .andExpect(flash().attribute("successMessage",
                        "If that account needs verifying, we've sent a fresh link to its email address."));

        verify(verificationService, never()).sendVerificationEmail(any());
    }

    @Test
    @WithMockUser
    void signInFailure_unverifiedTrue_withEmail_redirectsWithUnverifiedAndEmailQueryParam() throws Exception {
        mockMvc.perform(get("/sign-in-failure")
                        .param("unverified", "true")
                        .param("email", "john@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in?unverified=true&email=john@example.com"))
                .andExpect(flash().attribute("errorMessage", "Please verify your email before signing in."));
    }

    @Test
    @WithMockUser
    void signInFailure_unverifiedTrue_withoutEmail_redirectsWithUnverifiedQueryParamOnly() throws Exception {
        mockMvc.perform(get("/sign-in-failure")
                        .param("unverified", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in?unverified=true"))
                .andExpect(flash().attribute("errorMessage", "Please verify your email before signing in."));
    }

    @Test
    @WithMockUser
    void signInFailure_unverifiedTrue_withBlankEmail_redirectsWithUnverifiedQueryParamOnly() throws Exception {
        mockMvc.perform(get("/sign-in-failure")
                        .param("unverified", "true")
                        .param("email", "   "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sign-in?unverified=true"))
                .andExpect(flash().attribute("errorMessage", "Please verify your email before signing in."));
    }

}
