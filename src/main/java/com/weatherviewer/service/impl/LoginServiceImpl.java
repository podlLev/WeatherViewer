package com.weatherviewer.service.impl;

import com.weatherviewer.service.LoginService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * {@link LoginService} implementation that delegates to the Servlet API's
 * built-in {@link HttpServletRequest#login(String, String)}, which runs
 * credentials through the configured Spring Security
 * {@code AuthenticationManager} and establishes the session on success.
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final HttpServletRequest request;

    @Override
    public void login(String username, String password) throws ServletException {
        request.login(username, password);
    }

}
