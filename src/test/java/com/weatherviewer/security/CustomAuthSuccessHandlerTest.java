package com.weatherviewer.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthSuccessHandlerTest {

    @InjectMocks
    private CustomAuthSuccessHandler handler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectStrategy redirectStrategy;

    @Test
    void onAuthenticationSuccess_withRedirectParam_redirectsToUrl() throws IOException, ServletException {
        when(request.getParameter("redirect")).thenReturn("/dashboard");
        handler.setRedirectStrategy(redirectStrategy);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy).sendRedirect(request, response, "/dashboard");
    }

    @Test
    void onAuthenticationSuccess_withBlankRedirectParam_doesNotRedirect() throws IOException, ServletException {
        when(request.getParameter("redirect")).thenReturn("   ");
        handler.setRedirectStrategy(redirectStrategy);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy, never()).sendRedirect(any(), any(), eq("/dashboard"));
    }

    @Test
    void onAuthenticationSuccess_withNullRedirectParam_doesNotRedirectToCustomUrl() throws IOException, ServletException {
        when(request.getParameter("redirect")).thenReturn(null);
        handler.setRedirectStrategy(redirectStrategy);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy, never()).sendRedirect(any(), any(), eq("/dashboard"));
    }

    @Test
    void onAuthenticationSuccess_withUnsafeRedirectParam_logsWarningAndDoesNotRedirect() throws IOException, ServletException {
        String unsafeUrl = "https://malicious-site.com/steal-session";
        when(request.getParameter("redirect")).thenReturn(unsafeUrl);
        handler.setRedirectStrategy(redirectStrategy);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy, never()).sendRedirect(any(), any(), eq(unsafeUrl));
    }

    @Test
    void onAuthenticationSuccess_withAnotherUnsafeRedirectParam_doesNotRedirect() throws IOException, ServletException {
        String unsafeUrl = "//attacker.com";
        when(request.getParameter("redirect")).thenReturn(unsafeUrl);
        handler.setRedirectStrategy(redirectStrategy);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(redirectStrategy, never()).sendRedirect(any(), any(), eq(unsafeUrl));
    }

}
