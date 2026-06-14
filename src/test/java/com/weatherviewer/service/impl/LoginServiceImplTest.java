package com.weatherviewer.service.impl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LoginServiceImpl service;

    @Test
    void login_callsRequestLogin() throws ServletException {
        service.login("john@example.com", "Secure1@");

        verify(request).login("john@example.com", "Secure1@");
    }

    @Test
    void login_servletException_propagates() throws ServletException {
        doThrow(new ServletException("Login failed"))
                .when(request).login(anyString(), anyString());

        assertThatThrownBy(() -> service.login("john@example.com", "wrong"))
                .isInstanceOf(ServletException.class)
                .hasMessage("Login failed");
    }

}
