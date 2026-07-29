package com.weatherviewer.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CustomAuthFailureHandlerTest {

    private CustomAuthFailureHandler failureHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        failureHandler = new CustomAuthFailureHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void onAuthenticationFailure_badCredentials_delegatesToDefaultUrlWithoutUnverifiedParam()
            throws IOException, ServletException {
        AuthenticationException exception = new BadCredentialsException("Invalid credentials");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals("/sign-in-failure", response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationFailure_disabledException_redirectsWithUnverifiedAndEmailParam()
            throws IOException, ServletException {
        request.setParameter("email", "john@example.com");
        AuthenticationException exception = new DisabledException("User account is disabled");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals("/sign-in-failure?unverified=true&email=john@example.com", response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationFailure_disabledException_withoutEmail_redirectsWithUnverifiedParamOnly()
            throws IOException, ServletException {
        AuthenticationException exception = new DisabledException("User account is disabled");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals("/sign-in-failure?unverified=true", response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationFailure_disabledException_withoutEmail_redirectsWithoutEmailParam()
            throws IOException, ServletException {
        AuthenticationException exception = new DisabledException("Account is disabled");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals("/sign-in-failure?unverified=true", response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationFailure_lockedException_redirectsWithLockedAndEmailParam()
            throws IOException, ServletException {
        request.setParameter("email", "john@example.com");
        AuthenticationException exception = new LockedException("Account is locked");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals("/sign-in-failure?locked=true&email=john@example.com", response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationFailure_lockedException_withoutEmail_redirectsWithLockedParamOnly()
            throws IOException, ServletException {
        AuthenticationException exception = new LockedException("Account is locked");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals("/sign-in-failure?locked=true", response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationFailure_disabledException_withBlankEmail_redirectsWithUnverifiedParamOnly()
            throws IOException, ServletException {
        request.setParameter("email", "   ");
        AuthenticationException exception = new DisabledException("Account is disabled");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertEquals("/sign-in-failure?unverified=true", response.getRedirectedUrl());
    }

}
